# Otel Http Demo

Show that orchestration applied on http calls with OkHttp v3.0 from OpenTelemetry Instrumentation,
create isolated span.

# Instrumentation

* OpenTelemetry is created only one time in [application class](https://github.com/GerardPaligot/otel-demo/blob/main/app/src/main/java/com/decathlon/otel/demo/MainApplication.kt#L23-L36).
* `EventRepository` class is instrumented with a [custom span and a dedicated Tracer](https://github.com/GerardPaligot/otel-demo/blob/main/app/src/main/java/com/decathlon/otel/demo/data/EventRepository.kt#L13-L30).
* ktor is configured with [OkHttp client](https://github.com/GerardPaligot/otel-demo/blob/main/app/src/main/java/com/decathlon/otel/demo/data/ConferenceApi.kt#L27-L32) and instrumented with [OkHttp v3 OpenTelemetry](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/okhttp/okhttp-3.0/library).

## Logs

```bash
10:10:51.198  I  'HTTP GET' : 4ffc9e339210e71773a9dadf078cb166 ad1f4aa76ac095c3 CLIENT [tracer: io.opentelemetry.okhttp-3.0:1.22.0-alpha] AttributesMap{data={http.status_code=200, http.url=https://cms4partners-ce427.nw.r.appspot.com/events, http.method=GET, http.response_content_length=275, net.peer.name=cms4partners-ce427.nw.r.appspot.com, net.transport=ip_tcp, http.user_agent=Ktor client, http.flavor=2.0}, capacity=128, totalAddedValues=8}
10:10:51.221  I  'fetch-events' : cfba3500c76a78e1dffa5527a1d47dba 85bbba1bbf3c8a66 CLIENT [tracer: event-repository:] {}
```

## License

    Copyright 2023 Gérard Paligot.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
