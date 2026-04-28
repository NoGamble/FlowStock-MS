import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'

const { message } = createDiscreteApi(['message'])

const service = axios.create({
  baseURL: '',
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
    response => {
      const res = response.data;

      if (res.code !== 200) {
        message.error(res.message || '执行出错');
        return Promise.reject(new Error(res.message || 'Error'));
      }
      return res.data;
    },
    error => {
      console.error('网络请求异常:', error);
      message.error('网络连接失败，请检查后端服务是否启动');
      return Promise.reject(error);
    }
)

export default service
