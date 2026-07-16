package com.lab.governance.contract;

public record ServiceInstanceView(String serviceName, String ip, int port, boolean healthy, double weight,
                                  String cluster, String group) {
}
