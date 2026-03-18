import axios from 'axios'

// 假设我们会在 Naive UI 的外部使用 message，可以通过这种方式挂载全局的 message
// 为了简单起见，如果请求发生错误，直接使用 console.error 或者浏览器自带的 alert 也可以，
// 更好的做法是在 Vue 插件中注入专门处理的方法，这里提供基础 Axios 拦截

const service = axios.create({
  // Vite 配置的 proxy 前缀
  baseURL: '',
  timeout: 15000 
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 可以在这里统一加上 Token
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers['Authorization'] = `Bearer ${token}`
    // }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // Spring Boot 如果直接返回普通对象或字符串，这里直接将其解出即可
    return response.data;
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default service
