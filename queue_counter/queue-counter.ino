/*
********************************************
  Customer queue counter

  Author: Björn Makowe <bmakowe@users.noreply.github.com>
  Date: 28th May 2018

  Version: 1.0
********************************************
*/

#include <ESP8266WiFi.h>
#include <PubSubClient.h>

#define button         4 // D1


const char* wifi_ssid = "...";
const char* wifi_password = "...";

const char* broker_uri = "bonito";
const char* topic_customer = "/bonito/customers";

WiFiClient espClient;
PubSubClient client(espClient);


void setup() {
  pinMode(button, INPUT_PULLUP);

  Serial.begin(115200);

  setup_wifi();

  client.setServer(broker_uri, 1883);

  attachInterrupt(digitalPinToInterrupt(button), increment, CHANGE);
}

void increment() {
  client.publish(topic_customer, "+1");
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  
  client.loop();
}

void setup_wifi() {
  delay(10);

  Serial.println("Connecting to ");
  Serial.print(wifi_ssid);

  WiFi.begin(wifi_ssid, wifi_password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print("...");
  }

  randomSeed(micros());

  Serial.println("connected");

  Serial.println();

  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");

    if (client.connect(clientId.c_str())) {
      Serial.println("connected");

      client.subscribe(topic_customer);

    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");

      delay(5000);
    }
  }
}
