/**
 * 私信WebSocket客户端
 * <p>
 * 设计要点:
 * 1. 单例模式,全局只维护一个连接
 * 2. 自动重连(指数退避)
 * 3. 心跳保活(30秒)
 * 4. 支持多个消息监听器(用于不同页面接收消息)
 */
class ChatWebSocketClient {
  constructor() {
    this.ws = null
    this.url = null
    this.token = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10
    this.heartbeatTimer = null
    this.reconnectTimer = null
    this.listeners = new Set()
    this.isManualClose = false
  }

  /**
   * 连接WebSocket
   * @param {string} baseURL 后端基础地址(如 http://localhost:8080)
   * @param {string} token JWT token
   */
  connect(baseURL, token) {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return
    }
    this.token = token
    this.isManualClose = false
    // http(s):// -> ws(s)://
    const wsBaseURL = baseURL.replace(/^http/, 'ws')
    this.url = `${wsBaseURL}/ws/chat?token=${encodeURIComponent(token)}`
    this._createSocket()
  }

  _createSocket() {
    try {
      this.ws = new WebSocket(this.url)
    } catch (e) {
      console.error('WebSocket创建失败:', e)
      this._scheduleReconnect()
      return
    }

    this.ws.onopen = () => {
      console.log('[WS] 已连接')
      this.reconnectAttempts = 0
      this._startHeartbeat()
    }

    this.ws.onmessage = (event) => {
      if (event.data === 'pong') return
      try {
        const data = JSON.parse(event.data)
        this.listeners.forEach(fn => {
          try {
            fn(data)
          } catch (e) {
            console.error('[WS] 监听器执行异常:', e)
          }
        })
      } catch (e) {
        console.warn('[WS] 消息解析失败:', event.data)
      }
    }

    this.ws.onclose = (event) => {
      console.log('[WS] 已关闭:', event.code, event.reason)
      this._stopHeartbeat()
      if (!this.isManualClose) {
        this._scheduleReconnect()
      }
    }

    this.ws.onerror = (error) => {
      console.error('[WS] 错误:', error)
    }
  }

  /**
   * 指数退避重连
   */
  _scheduleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.warn('[WS] 已达最大重连次数,停止重连')
      return
    }
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)
    this.reconnectAttempts++
    console.log(`[WS] ${delay}ms 后第 ${this.reconnectAttempts} 次重连`)
    this.reconnectTimer = setTimeout(() => {
      if (this.token) {
        this._createSocket()
      }
    }, delay)
  }

  _startHeartbeat() {
    this._stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        try {
          this.ws.send('ping')
        } catch (e) {
          console.warn('[WS] 心跳发送失败:', e)
        }
      }
    }, 30000)
  }

  _stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 注册消息监听器
   * @param {(msg: object) => void} listener
   * @returns {() => void} 取消监听函数
   */
  onMessage(listener) {
    this.listeners.add(listener)
    return () => this.listeners.delete(listener)
  }

  /**
   * 主动断开连接
   */
  disconnect() {
    this.isManualClose = true
    this._stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      try {
        this.ws.close()
      } catch (e) {
        // ignore
      }
      this.ws = null
    }
    this.listeners.clear()
  }

  /**
   * 是否已连接
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

// 单例
export const chatWebSocket = new ChatWebSocketClient()

/**
 * 根据当前环境返回后端基础地址
 */
export function getBackendBaseURL() {
  // 开发环境通过vite代理,WebSocket需要直连后端
  const { protocol, hostname } = window.location
  // 开发环境: 前端3000,后端8080
  if (window.location.port === '3000') {
    return `${protocol}//${hostname}:8080`
  }
  // 生产环境: 同源
  return `${protocol}//${hostname}${window.location.port ? ':' + window.location.port : ''}`
}
