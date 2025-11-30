/**
 * 防抖和节流工具函数
 */

/**
 * 防抖函数
 * @param {Function} fn 要执行的函数
 * @param {number} delay 延迟时间（毫秒）
 * @param {boolean} immediate 是否立即执行
 * @returns {Function} 防抖后的函数
 */
export function debounce(fn, delay = 300, immediate = false) {
  let timer = null
  
  return function(...args) {
    const context = this
    
    if (immediate && !timer) {
      fn.apply(context, args)
    }
    
    clearTimeout(timer)
    timer = setTimeout(() => {
      if (!immediate) {
        fn.apply(context, args)
      }
      timer = null
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} fn 要执行的函数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(fn, delay = 300) {
  let lastTime = 0
  
  return function(...args) {
    const context = this
    const now = Date.now()
    
    if (now - lastTime >= delay) {
      fn.apply(context, args)
      lastTime = now
    }
  }
}

/**
 * 防抖装饰器（用于Vue组件）
 */
export function useDebounce(fn, delay = 300) {
  return debounce(fn, delay)
}

/**
 * 节流装饰器（用于Vue组件）
 */
export function useThrottle(fn, delay = 300) {
  return throttle(fn, delay)
}

