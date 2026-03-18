package com.paligot.otel.demo.otel

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.Severity

fun Logger.log(
    message: String,
    severity: Severity = Severity.INFO,
    attributes: Attributes = Attributes.empty(),
) {
    this.logRecordBuilder()
        .setSeverity(severity)
        .setBody(message)
        .setAllAttributes(attributes)
        .emit()
}
