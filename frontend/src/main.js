import { createApp } from 'vue';
import { createPinia } from 'pinia';
// 按需引入：只挂本项目模板里真正用到的组件。
// 全量 app.use(ElementPlus) 会把整个组件库打进包（压缩后 1MB+，隧道里加载很慢）。
// 以后模板里用到新的 el-xxx 组件，要在这里补一行，否则运行时报“组件未解析”。
import {
  ElButton,
  ElDatePicker,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElLoadingDirective,
  ElOption,
  ElSelect
} from 'element-plus';

import 'element-plus/dist/index.css';
import '@fontsource-variable/archivo';
import './styles/tokens.css';
import './styles/base.css';

import App from './App.vue';
import router from './router';

const app = createApp(App);

app.use(createPinia());
app.use(router);

// 组件名用 PascalCase 注册，模板里写 kebab-case（<el-button>）一样能解析
[
  ElButton,
  ElDatePicker,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElOption,
  ElSelect
].forEach((component) => app.component(component.name, component));

// v-loading：几个列表页用它显示加载态
app.directive('loading', ElLoadingDirective);

app.mount('#app');
