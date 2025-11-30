import {defineStore} from "pinia";
import {ref} from "vue";

/**
 * 清理token格式，确保是纯字符串
 * @param {any} tokenValue - token值
 * @returns {string} 清理后的token字符串
 */
function cleanToken(tokenValue) {
  if (!tokenValue) return '';
  
  // 如果是字符串，直接清理
  if (typeof tokenValue === 'string') {
    return tokenValue.trim();
  }
  
  // 如果是对象，尝试提取token字段
  if (typeof tokenValue === 'object') {
    const tokenStr = tokenValue.token || tokenValue.value || tokenValue.data;
    if (tokenStr && typeof tokenStr === 'string') {
      return tokenStr.trim();
    }
  }
  
  // 其他情况返回空字符串
  return '';
}

export const useTokenStore = defineStore('token',
    () => {
      const token = ref('')

      /**
       * 设置token，自动清理格式
       * @param {string} newToken - 新的token值
       */
      const setToken = (newToken) => {
        const cleanedToken = cleanToken(newToken);
        token.value = cleanedToken;
      }

      /**
       * 移除token
       */
      const removeToken = () => {
        token.value = '';
      }

      return {
        token,
        setToken, 
        removeToken,
        // 提供一个getter方法确保返回清理后的token
        get cleanToken() {
          return cleanToken(token.value);
        },
        // 检查是否有有效的token
        get hasToken() {
          const t = cleanToken(token.value);
          return t && t.length > 0;
        }
      }
    },
    {
      persist: {
        key: 'token',
        storage: localStorage,
        // 自定义序列化，确保存储为字符串
        serializer: {
          serialize: (value) => {
            const tokenValue = value?.token || value || '';
            return typeof tokenValue === 'string' ? tokenValue : '';
          },
          deserialize: (value) => {
            // 如果存储的是字符串，直接返回
            if (typeof value === 'string') {
              return { token: value.trim() };
            }
            // 如果是JSON对象，尝试解析
            try {
              const parsed = typeof value === 'string' ? JSON.parse(value) : value;
              if (parsed && typeof parsed === 'object' && parsed.token) {
                return { token: parsed.token.trim() };
              }
            } catch (e) {
              // 解析失败，返回空token
            }
            return { token: '' };
          }
        }
      }
    });