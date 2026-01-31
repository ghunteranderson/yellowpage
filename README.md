# Yellowpage
Yellowpage is a lighweight minimal DNS server for home labs and other non critical infrastructure.

Yellowpage supports YAML zone files for local name resolution and query forwarding for everything else.
Currently, Yellowpage does not support TCP or DNSSEC.
Prometheus-style metrics are exposed for monitoring and alerting.

## Running

Either download yellowpage from that latest release or build from source with `mvn package -Dnative`.
The installation is a single binary.

Run yellowpage with `./yellowpage`. For available options, run `./yellowpage --help` or reference the table below.

| CLI Option | Env Variable | Default | Description |
|------------|--------------|---------|-------------|
| `--ip`     | `YP_IP`        | `0.0.0.0` | The IP that the Yellowpage DNS listener should bind to. |
| `--port`   | `YP_PORT`      | `53`      | The port that the Yellowpage DNS listener should bind to. |
| `--zones`  | `YP_ZONES`     | `/etc/yellowpage/zones.d/` | A comma separate list of directories of files where YAML zone files may be found. |
| `--forward.ip` | `YP_FORWARD_IP` | `1.1.1.1` | The IP address of the upstream DNS server. This is used when queried names do not match a zone file. |
| `--forward.port` | `YP_FORWARD_PORT` | `53` | The port of the upstream DNS server. See `--forward.ip`. |
| `--metrics.enabled` | `YP_METRICS_ENABLED` | `true` | Enables Prometheus style metrics server at `http://localhost:{metrics.port}/metrics`. |
| `--metrics.port`    | `YP_METRICS_PORT` | `9053` | Port of metrics HTTP server. See `--metrics.enabled` |
| `--help`            | - | - | Diplays available CLI options. Does not start the server. |

## Zone Files

Zone files contain the IP address records for local domains.
More generally, zone files tell Yellowpage which set of domains it can answer for.
Domains that to not match a zone are forwarded (see `--forward.ip`).
If a domain matches a zone's name, it will not be forwarded even if there are no address records matching the client's query. 

Zone files are written in YAML and their location is configured with option `--zones` or env `YP_ZONES`.
Changes to a zone file are automatically reloaded without restarting Yellowpage.

```yml
zone: lab.internal
ttl: 3600
records:
  - { name: "@",      type: A, value: 10.0.0.11 } # lab.internal -> 10.0.0.11
  - { name: "node-1", type: A, value: 10.0.0.12 } # node-1.lab.internal -> 10.0.0.12
  - { name: "*.svc",  type: A, value: 10.0.0.13 } # prometheus.svc.lab.internal -> 10.0.0.13
  - { name: "*",      type: A, value: 10.0.0.14 } # grafana.lab.internal -> 10.0.0.14
```

## Deploying

It's recommended to deploy Yellowpage as a service.
Below is an example systemd unit file.
It uses a system user, yellowpage, with the executable installed at `/opt/yellowpage/bin/yellowpage`.

```ini
[Unit]
Description=Yellowpage DNS
Wants=network-online.target
After=network-online.target
Wants=nss-lookup.target
Before=nss-lookup.target

[Service]
User=yellowpage
AmbientCapabilities=CAP_NET_BIND_SERVICE
ExecStart=/opt/yellowpage/bin/yellowpage
Restart=on-failure
ProtectSystem=full
```


Zone files should be stored in `/etc/yellowpage/zones.d` unless configured otherwise.
Be sure to restrict write access to all zone paths.