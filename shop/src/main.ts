import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 全局样式
import './styles/reset.css'
import './styles/global.css'

// Vant 样式 + 组件
import 'vant/lib/index.css'
import {
  Swipe, SwipeItem, NavBar, Tabbar, TabbarItem,
  Search, Card, Button, Tag, Badge, Empty, Skeleton,
  Toast, Dialog, Image as VanImage, Stepper,
  SubmitBar, Checkbox, CheckboxGroup, SwipeCell,
  Tabs, Tab, PullRefresh, List, Form, Field,
  ActionSheet, Uploader,
  Icon, Cell, CellGroup, Switch, Popup, AddressList
} from 'vant'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 初始化登录态（通过 HttpOnly Cookie 校验）
import { useUserStore } from '@/stores/useUserStore'
const userStore = useUserStore()
userStore.init()

// Vant 组件注册
const vantComponents = [
  Swipe, SwipeItem, NavBar, Tabbar, TabbarItem,
  Search, Card, Button, Tag, Badge, Empty, Skeleton,
  VanImage, Stepper, SubmitBar, Checkbox, CheckboxGroup, SwipeCell,
  Tabs, Tab, PullRefresh, List, Form, Field,
  ActionSheet, Uploader,
  Icon, Cell, CellGroup, Switch, Popup, AddressList
]
vantComponents.forEach(comp => app.use(comp))
app.use(Dialog)

// Toast 全局方法
app.config.globalProperties.$toast = Toast
app.config.globalProperties.$dialog = Dialog

app.mount('#app')
