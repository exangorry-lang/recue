// 请求封装（uni.request）
// H5 端：按当前访问主机动态拼后端地址（电脑/手机局域网都能连）
// APP 端优先使用打包环境变量 VITE_API_BASE_URL，也可由运维预置 apiBaseUrl 本地配置。
// 不再把某台开发电脑的局域网 IP 固化进安装包。
let BASE_URL = uni.getStorageSync('apiBaseUrl') || import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080'
// #ifdef H5
BASE_URL = `http://${window.location.hostname}:8080`
// #endif

export function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    const header = { 'Content-Type': 'application/json' }
    if (token) {
      header.Authorization = 'Bearer ' + token
    }
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        const body = res.data
        if (res.statusCode === 200 && body && body.code === 200) {
          resolve(body)
        } else if (body && body.code === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('user')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error((body && body.msg) || '未登录'))
        } else {
          reject(new Error((body && body.msg) || '请求失败'))
        }
      },
      fail: () => reject(new Error('网络错误，请检查连接'))
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
