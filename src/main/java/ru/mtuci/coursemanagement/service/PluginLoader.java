package ru.mtuci.coursemanagement.plugin;

public class PluginLoader {

    public interface Plugin {
        String execute();
    }

    public static class HelloPlugin implements Plugin {
        public String execute() {
            return "Hello from plugin";
        }
    }

    public static class InfoPlugin implements Plugin {
        public String execute() {
            return "Plugin system is safe now";
        }
    }

    public Plugin load(String pluginName) {

        if ("hello".equals(pluginName)) {
            return new HelloPlugin();
        }

        if ("info".equals(pluginName)) {
            return new InfoPlugin();
        }

        throw new IllegalArgumentException("Plugin is not allowed");
    }
}
