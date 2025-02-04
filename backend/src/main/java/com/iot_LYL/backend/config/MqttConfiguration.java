package com.iot_LYL.backend.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT配置类
 * 负责创建和配置MQTT客户端连接
 * 配置项包括：
 * - 服务器地址
 * - 客户端ID
 * - 自动重连
 * - 会话清理
 */
@Configuration
public class MqttConfiguration {
    
    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.topic}")
    private String topic;

    /**
     * 创建MQTT客户端
     */
    @Bean
    public MqttClient mqttClient() throws MqttException {
        // 创建MQTT客户端，使用固定的客户端ID
        MqttClient client = new MqttClient(brokerUrl, "mqtt-temp-monitor");
        
        // 配置MQTT连接选项
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);  // 自动重连
        options.setCleanSession(true);        // 清除会话
        
        // 连接到MQTT服务器
        client.connect(options);
        
        return client;
    }
} 