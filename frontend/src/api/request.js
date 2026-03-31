import axios from 'axios'

// 假设我们会在 Naive UI 的外部使用 message，可以通过这种方式挂载全局的 message
// 为了简单起见，如果请求发生错误，直接使用 console.error 或者浏览器自带的 alert 也可以，
// 更好的做法是在 Vue 插件中注入专门处理的方法，这里提供基础 Axios 拦截

const service = axios.create({
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
      const res = response.data;

      if (res.code !== 200) {
        alert(res.message || '执行出错');

        return Promise.reject(new Error(res.message || 'Error'));
      }
      return res.data;
    },
    error => {
      console.error('网络请求异常:', error);
      alert('网络连接失败，请检查后端服务是否启动');
      return Promise.reject(error);
    }
)

export default service
