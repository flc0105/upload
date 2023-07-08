import { createI18n } from "vue-i18n";

const browserLocale = navigator.language; // 获取浏览器默认语言设置
import zh from "../locales/zh";
import en from "../locales/en";

const i18n = createI18n({
  // locale: 'zh', // 默认语言设置为中文
  locale: browserLocale,
  fallbackLocale: "en",
  messages: {
    zh,
    en,
  },
});

export default i18n;
