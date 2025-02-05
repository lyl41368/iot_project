package com.iot_LYL.backend.service;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Modbus查询服务
 * 负责定时发送Modbus查询指令到设备
 * 通过MQTT协议发送以下查询指令：
 * 1. 暖气温度查询: 01 03 00 00 00 02 CRC
 * 2. 室内环境查询: 02 04 00 00 00 02 CRC
 */
@Service
public class ModbusQueryService {

    @Autowired
    private MqttClient mqttClient;

    @Value("${mqtt.query.heater.interval}")
    private long heaterQueryInterval;

    @Value("${mqtt.query.room.interval}")
    private long roomQueryInterval;

    // 查询暖气温度的指令
    private final byte[] HEATER_QUERY = new byte[]{0x01, 0x03, 0x00, 0x00, 0x00, 0x02, (byte)0xC4, 0x0B};
    // 查询室内温湿度的指令
    private final byte[] ROOM_QUERY = new byte[]{0x02, 0x04, 0x00, 0x00, 0x00, 0x02, 0x71, (byte)0xF8};

    /**
     * 查询暖气温度
     * 发送Modbus指令：01 03 00 00 00 02 C4 0B
     * - 01: 设备地址
     * - 03: 功能码(读保持寄存器)
     * - 0000: 起始寄存器
     * - 0002: 寄存器数量
     * - C40B: CRC校验
     */
    @Scheduled(fixedRateString = "${mqtt.query.heater.interval}")
    public void queryHeaterTemperature() {
        try {
            MqttMessage message = new MqttMessage(HEATER_QUERY);
            mqttClient.publish("D4AD20AB8F7E/sub", message);
            System.out.println("Sent heater query command");
            System.out.println("Heater query interval: " + heaterQueryInterval);
        } catch (Exception e) {
            System.err.println("Error querying heater temperature: " + e.getMessage());
        }
    }

    /**
     * 查询室内环境（温度和湿度）
     * 发送Modbus指令：02 04 00 00 00 02 71 F8
     * - 02: 设备地址（室内环境传感器）
     * - 04: 功能码(读输入寄存器)
     * - 0000: 起始寄存器地址
     * - 0002: 读取2个寄存器（温度和湿度）
     * - 71F8: CRC校验
     * 
     * 响应数据格式：02 04 04 XXXX YYYY CRC
     * - 02: 设备地址
     * - 04: 功能码
     * - 04: 字节数
     * - XXXX: 温度值（需除以10）
     * - YYYY: 湿度值（需除以10）
     */
    @Scheduled(fixedRateString = "${mqtt.query.room.interval}")
    public void queryRoomEnvironment() {
        try {
            MqttMessage message = new MqttMessage(ROOM_QUERY);
            mqttClient.publish("D4AD20AB8F7E/sub", message);
            System.out.println("Sent room environment query command");
            System.out.println("Room query interval: " + roomQueryInterval);
        } catch (Exception e) {
            System.err.println("Error querying room environment: " + e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        System.out.println("ModbusQueryService initialized with intervals:");
        System.out.println("Heater query interval: " + heaterQueryInterval);
        System.out.println("Room query interval: " + roomQueryInterval);
    }
} 