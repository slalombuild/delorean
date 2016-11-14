package com.slalom.autoconfiguration.delorean;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "slalom.delorean")
public class TimeMachineConfigurationProperties {

    private static final String TIME_MACHINE_HEADER = "X-Delorean-Time-Machine";
    private static final String TIME_MACHINE_COOKIE = "Delorean-Time-Machine";
    private static final String PATH = "time-machine";

    private boolean enabled = true;
    private String headerName = TIME_MACHINE_HEADER;

    private final OutboundHeader outboundRequestHeader = new OutboundHeader();
    private final CookieTestDateProvider cookie = new CookieTestDateProvider();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(final String headerName) {
        this.headerName = headerName;
    }

    public OutboundHeader getOutboundRequestHeader() {
        return outboundRequestHeader;
    }

    public CookieTestDateProvider getCookie() {
        return cookie;
    }

    public static class OutboundHeader {
        private boolean enabled = true;
        private String name = TIME_MACHINE_HEADER;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    public static class CookieTestDateProvider {
        private boolean enabled = false;
        private String name = TIME_MACHINE_COOKIE;
        private String path = PATH;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(final String path) {
            this.path = path;
        }
    }
}
