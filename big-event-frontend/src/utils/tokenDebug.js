/**
 * Token诊断工具
 * 用于调试和排查token相关问题
 */

import { useTokenStore } from '@/stores/token';

/**
 * 诊断token状态
 * @returns {Object} 诊断结果
 */
export function diagnoseToken() {
  const tokenStore = useTokenStore();
  const token = tokenStore.token;
  
  const diagnosis = {
    hasToken: !!token && token.trim().length > 0,
    tokenLength: token ? token.length : 0,
    tokenPrefix: token && token.length > 20 ? token.substring(0, 20) + '...' : token || 'N/A',
    tokenFormat: 'unknown',
    localStorage: {},
    issues: []
  };

  // 检查token格式
  if (token) {
    const trimmed = token.trim();
    if (trimmed.includes('.') && trimmed.split('.').length === 3) {
      diagnosis.tokenFormat = 'JWT (valid format)';
    } else {
      diagnosis.tokenFormat = 'Invalid (not a valid JWT)';
      diagnosis.issues.push('Token格式不正确，不是有效的JWT格式');
    }
    
    // 检查token长度（JWT通常很长）
    if (trimmed.length < 50) {
      diagnosis.issues.push('Token长度异常，JWT token通常长度大于50字符');
    }
    
    // 检查是否包含空格或换行
    if (token !== trimmed) {
      diagnosis.issues.push('Token包含前后空格或换行符');
    }
    if (token.includes('\n') || token.includes('\r') || token.includes('\t')) {
      diagnosis.issues.push('Token包含换行符或制表符');
    }
  } else {
    diagnosis.issues.push('Token为空');
  }

  // 检查localStorage
  try {
    const stored = localStorage.getItem('token');
    if (stored) {
      diagnosis.localStorage = {
        exists: true,
        type: typeof stored,
        length: stored.length,
        isJSON: false,
        content: stored.length > 50 ? stored.substring(0, 50) + '...' : stored
      };
      
      // 尝试解析JSON
      try {
        const parsed = JSON.parse(stored);
        diagnosis.localStorage.isJSON = true;
        diagnosis.localStorage.parsedType = typeof parsed;
        if (typeof parsed === 'object') {
          diagnosis.localStorage.keys = Object.keys(parsed);
        }
      } catch (e) {
        diagnosis.localStorage.isJSON = false;
      }
    } else {
      diagnosis.localStorage.exists = false;
    }
  } catch (e) {
    diagnosis.localStorage.error = e.message;
  }

  // 检查所有localStorage中的token相关项
  try {
    const allTokenKeys = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.toLowerCase().includes('token')) {
        allTokenKeys.push(key);
      }
    }
    diagnosis.localStorage.allTokenKeys = allTokenKeys;
  } catch (e) {
    // 忽略错误
  }

  return diagnosis;
}

/**
 * 打印token诊断信息到控制台
 */
export function printTokenDiagnosis() {
  if (!import.meta.env.DEV) {
    console.warn('Token诊断工具仅在开发环境下可用');
    return;
  }

  const diagnosis = diagnoseToken();
  
  console.group('🔍 Token诊断信息');
  console.log('Token状态:', {
    '是否有Token': diagnosis.hasToken ? '✅ 是' : '❌ 否',
    'Token长度': diagnosis.tokenLength,
    'Token前缀': diagnosis.tokenPrefix,
    'Token格式': diagnosis.tokenFormat
  });
  
  if (diagnosis.issues.length > 0) {
    console.warn('⚠️ 发现的问题:');
    diagnosis.issues.forEach(issue => {
      console.warn('  -', issue);
    });
  } else {
    console.log('✅ 未发现问题');
  }
  
  console.log('LocalStorage状态:', diagnosis.localStorage);
  
  if (diagnosis.localStorage.allTokenKeys && diagnosis.localStorage.allTokenKeys.length > 0) {
    console.log('所有Token相关的LocalStorage键:', diagnosis.localStorage.allTokenKeys);
  }
  
  console.groupEnd();
  
  return diagnosis;
}

/**
 * 清理token（用于测试）
 */
export function clearTokenForTesting() {
  if (!import.meta.env.DEV) {
    console.warn('Token清理工具仅在开发环境下可用');
    return;
  }
  
  const tokenStore = useTokenStore();
  tokenStore.removeToken();
  console.log('✅ Token已清除');
}

/**
 * 验证token格式
 * @param {string} token - 要验证的token
 * @returns {Object} 验证结果
 */
export function validateTokenFormat(token) {
  const result = {
    isValid: false,
    isJWT: false,
    issues: []
  };

  if (!token) {
    result.issues.push('Token为空');
    return result;
  }

  const trimmed = token.trim();
  
  // 检查JWT格式（应该包含两个点）
  const parts = trimmed.split('.');
  if (parts.length === 3) {
    result.isJWT = true;
    result.isValid = true;
  } else {
    result.issues.push('Token不是有效的JWT格式（应该包含两个点分隔符）');
  }

  // 检查长度
  if (trimmed.length < 50) {
    result.issues.push('Token长度异常（JWT通常长度大于50字符）');
    result.isValid = false;
  }

  // 检查特殊字符
  if (token !== trimmed) {
    result.issues.push('Token包含前后空格');
  }
  if (token.includes('\n') || token.includes('\r') || token.includes('\t')) {
    result.issues.push('Token包含换行符或制表符');
    result.isValid = false;
  }

  return result;
}

// 开发环境下自动打印诊断信息（可选）
if (import.meta.env.DEV) {
  // 可以在需要时调用 printTokenDiagnosis() 来查看诊断信息
  // 或者添加到全局对象方便调试
  if (typeof window !== 'undefined') {
    window.tokenDebug = {
      diagnose: diagnoseToken,
      print: printTokenDiagnosis,
      clear: clearTokenForTesting,
      validate: validateTokenFormat
    };
    console.log('💡 Token诊断工具已加载，使用 window.tokenDebug.print() 查看诊断信息');
  }
}

