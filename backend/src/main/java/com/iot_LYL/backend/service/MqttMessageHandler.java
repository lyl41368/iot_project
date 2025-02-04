package com.iot_LYL.backend.service;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * MQTT消息处理服务
 * 负责接收MQTT消息，解析温度数据并存储到MongoDB
 * 处理两种类型的传感器数据：
 * 1. 暖气温度传感器 (0x01 0x03): 进水温度和回水温度
 * 2. 室内环境传感器 (0x02 0x04): 室温和湿度
 */
@Service
public class MqttMessageHandler implements MqttCallback {

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${mqtt.topic}")
    private String topic;

    /**
     * 初始化MQTT订阅
     * 在服务启动时自动执行：
     * 1. 设置消息回调处理
     * 2. 订阅指定主题
     * 
     * @throws MqttException MQTT连接或订阅异常
     */
    @PostConstruct
    public void init() throws MqttException {
        mqttClient.setCallback(this);
        mqttClient.subscribe(topic);
    }

    @Override
    public void connectionLost(Throwable cause) {
        // 连接丢失时的处理
        System.out.println("Connection lost: " + cause.getMessage());
    }

    /**
     * 解析Modbus响应数据
     * 数据格式：
     * - 暖气温度: 0103 04 XXXX YYYY CRC
     *   XXXX: 进水温度 (需除以10)
     *   YYYY: 回水温度 (需除以10)
     * - 室内环境: 0204 04 XXXX YYYY CRC
     *   XXXX: 室温 (需除以10)
     *   YYYY: 湿度 (需除以10)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            byte[] payload = message.getPayload();
            String hexString = bytesToHex(payload);
            System.out.println("Received message (HEX): " + hexString);
            
            // 解析Modbus数据
            if (hexString.startsWith("0103")) {  // 温度传感器1的数据
                double temp1 = Math.round(Integer.parseInt(hexString.substring(6, 10), 16) / 10.0 * 10) / 10.0;
                double temp2 = Math.round(Integer.parseInt(hexString.substring(10, 14), 16) / 10.0 * 10) / 10.0;
                
                saveTemperature("inlet", temp1);
                saveTemperature("outlet", temp2);
                System.out.println("Saving temperature: " + temp1);
                System.out.println("Saving temperature: " + temp2);

            } else if (hexString.startsWith("0204")) {  // 温湿度传感器的数据
                double temp = Math.round(Integer.parseInt(hexString.substring(6, 10), 16) / 10.0 * 10) / 10.0;
                double humidity = Math.round(Integer.parseInt(hexString.substring(10, 14), 16) / 10.0 * 10) / 10.0;
                
                saveTemperature("room", temp);
                saveHumidity(humidity);
                
                // 添加日志输出，帮助调试
                System.out.println("Saving humidity: " + humidity);
                System.out.println("Saving temperature: " + temp);
            }
            

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();  // 添加详细的错误信息
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // 消息发送完成的回调（本例中用不到）
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 用于将接收到的Modbus响应数据转换为可读格式
     * 
     * 示例：
     * 输入: [0x01, 0x03, 0x04, 0x01, 0x92, 0x01, 0x4C]
     * 输出: "0103040192014C"
     * 
     * @param bytes 需要转换的字节数组
     * @return 十六进制字符串（大写）
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    /**
     * 保存温度数据到MongoDB
     * 数据格式：
     * {
     *   "type": "inlet/outlet/room",  // 温度点类型
     *   "temperature": xx.x,          // 温度值（保留一位小数）
     *   "timestamp": ISODate()        // 存储时间（北京时间）
     * }
     * 
     * @param type 温度类型：inlet(进水)/outlet(回水)/room(室温)
     * @param temperature 温度值，精确到小数点后一位
     */
    private void saveTemperature(String type, double temperature) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("temperature", temperature);
        // 使用北京时间
        data.put("timestamp", new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000));
        
        mongoTemplate.save(data, "sensor_temperature");
    }

    /**
     * 保存湿度数据到MongoDB
     * 数据格式：
     * {
     *   "type": "room",              // 固定为room类型
     *   "humidity": xx.x,            // 湿度值（保留一位小数）
     *   "timestamp": ISODate()       // 存储时间（北京时间）
     * }
     * 
     * @param humidity 湿度值，精确到小数点后一位
     */
    private void saveHumidity(double humidity) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "room");           // 添加类型标识
        data.put("humidity", humidity);
        // 使用北京时间
        data.put("timestamp", new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000));
        
        mongoTemplate.save(data, "sensor_humidity");
    }
} 