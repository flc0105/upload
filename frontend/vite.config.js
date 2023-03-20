import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import legacy from "@vitejs/plugin-legacy";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    legacy({
      targets: [
        "> 1%, last 1 version, ie >= 11",
        "safari >= 10",
        "Android > 39",
        "Chrome >= 60",
        "Safari >= 10.1",
        "iOS >= 10.3",
        "Firefox >= 54",
        "Edge >= 15",
      ],
      additionalLegacyPolyfills: ["regenerator-runtime/runtime"],
      polyfills: ["es.promise.finally", "es/map", "es/set"],
      modernPolyfills: ["es.promise.finally"],
    }),
  ],
  build: {
    target: "es2015",
  },
  server: {
    host: true,
  },
});
