import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

/* 全局样式 */
import 'ant-design-vue/dist/reset.css'
import './styles/index.less'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(Antd)

// 登录态由路由守卫懒初始化（避免阻塞 app 挂载）
app.mount('#app')
