<template>
  <div class="login-container">
    <!-- 背景粒子效果 -->
    <div class="particles">
      <div v-for="particle in particles" :key="particle.id" 
           class="particle"
           :style="{
             left: particle.x + '%',
             top: particle.y + '%',
             animationDelay: particle.delay + 's',
             animationDuration: particle.duration + 's',
             width: particle.size + 'px',
             height: particle.size + 'px'
           }">
      </div>
    </div>
    
    <!-- 动态网格背景 -->
    <div class="grid-bg"></div>
    
    <!-- 光轨效果 -->
    <div class="light-orbit orbit-1"></div>
    <div class="light-orbit orbit-2"></div>
    
    <div class="login-box">
      <!-- Logo区域 -->
      <div class="logo-section">
        <img src="/logo.png" alt="财报智析" class="logo-image" />
      </div>
      
      <!-- 标题 -->
      <h2 class="title">用户登录</h2>
      
      <!-- 角色选择器 -->
      <div class="role-selector">
        <span class="role-label">选择角色：</span>
        <div class="role-options">
          <label :class="['role-option', { active: loginForm.role === 'user' }]">
            <input type="radio" v-model="loginForm.role" value="user" />
            <span class="role-icon">👤</span>
            <span class="role-text">普通用户</span>
          </label>
          <label :class="['role-option', { active: loginForm.role === 'admin' }]">
            <input type="radio" v-model="loginForm.role" value="admin" />
            <span class="role-icon">🛡️</span>
            <span class="role-text">管理员</span>
          </label>
        </div>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0" class="login-form">
        <el-form-item prop="loginName">
          <div class="input-wrapper">
            <el-input
              v-model="loginForm.loginName"
              placeholder="请输入用户名/邮箱/手机号"
              prefix-icon="User"
              size="large"
              class="custom-input"
            />
            <div class="input-glow"></div>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <div class="input-wrapper">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
              class="custom-input"
            />
            <div class="input-glow"></div>
          </div>
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-wrapper">
            <div class="input-wrapper captcha-input">
              <el-input
                v-model="loginForm.captcha"
                placeholder="请输入验证码"
                size="large"
                class="custom-input captcha-field"
                @keyup.enter="handleLogin"
              />
              <div class="input-glow"></div>
            </div>
            <img 
              :src="captchaImg" 
              alt="验证码" 
              class="captcha-img"
              @click="refreshCaptcha"
            />
          </div>
        </el-form-item>
        <el-form-item class="btn-item">
          <el-button type="primary" size="large" @click="handleLogin" :loading="loading" class="login-btn">
            <span class="btn-text">登 录</span>
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Key } from '@element-plus/icons-vue'
import { login, getCaptcha } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')

const loginForm = reactive({
  loginName: '',
  password: '',
  captcha: '',
  uuid: '',
  role: 'user'
})

const particles = ref([])

// 生成粒子数据
const generateParticles = () => {
  const count = 50
  for (let i = 0; i < count; i++) {
    particles.value.push({
      id: i,
      x: Math.random() * 100,
      y: Math.random() * 100,
      delay: Math.random() * 5,
      duration: 3 + Math.random() * 4,
      size: 2 + Math.random() * 4
    })
  }
}

const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    if (res.code === 200) {
      loginForm.uuid = res.data.uuid
      captchaImg.value = res.data.image
    } else {
      ElMessage.error('验证码获取失败')
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('验证码获取失败')
  }
}

onMounted(() => {
  generateParticles()
  refreshCaptcha()
})

const rules = {
  loginName: [
    { required: true, message: '请输入用户名/邮箱/手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        userStore.setToken(res.data.token)
        userStore.setUserInfo(res.data)
        ElMessage.success('登录成功')
        
        // 根据数据库中的角色跳转到相应页面
        const userRole = res.data.role
        if (userRole === 'admin') {
          router.push('/admin/users')
        } else {
          router.push('/home')
        }
      } catch (error) {
        console.error('登录失败:', error)
        // 错误消息已由响应拦截器显示，此处不再重复显示
        // 登录失败后刷新验证码
        refreshCaptcha()
        loginForm.captcha = ''
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #0d1117 50%, #0a0a0a 100%);
  position: relative;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

/* 粒子效果 */
.particles {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: radial-gradient(circle, rgba(2, 229, 180, 0.8) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 4s ease-in-out infinite;
  box-shadow: 0 0 10px rgba(2, 229, 180, 0.5), 0 0 20px rgba(2, 229, 180, 0.3);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-20px) scale(1.2);
    opacity: 1;
  }
}

/* 动态网格背景 */
.grid-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(2, 229, 180, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(2, 229, 180, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 50px 50px;
  }
}

/* 光轨效果 */
.light-orbit {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(2, 229, 180, 0.1);
  animation: orbit 20s linear infinite;
}

.orbit-1 {
  width: 800px;
  height: 800px;
  top: -300px;
  right: -200px;
  animation-duration: 35s;
}

.orbit-2 {
  width: 500px;
  height: 500px;
  bottom: -200px;
  left: -100px;
  animation-duration: 30s;
  animation-direction: reverse;
}

.orbit-1::before,
.orbit-2::before {
  content: '';
  position: absolute;
  width: 12px;
  height: 12px;
  background: rgba(2, 229, 180, 0.8);
  border-radius: 50%;
  box-shadow: 0 0 25px rgba(2, 229, 180, 0.8), 0 0 50px rgba(2, 229, 180, 0.4);
}

.orbit-1::before {
  top: 50%;
  right: 0;
  transform: translateY(-50%);
}

.orbit-2::before {
  bottom: 50%;
  left: 0;
  transform: translateY(50%);
}

@keyframes orbit {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.login-box {
  width: 480px;
  padding: 55px;
  background: rgba(15, 15, 20, 0.96);
  border-radius: 20px;
  box-shadow: 
    0 0 60px rgba(2, 229, 180, 0.2),
    0 0 120px rgba(2, 229, 180, 0.08),
    inset 0 0 80px rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(2, 229, 180, 0.25);
  position: relative;
  z-index: 10;
  backdrop-filter: blur(15px);
  animation: fadeInUp 0.6s ease-out;
  box-sizing: border-box;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Logo区域 */
.logo-section {
  text-align: center;
  margin-bottom: 35px;
  padding-bottom: 30px;
  border-bottom: 1px solid rgba(2, 229, 180, 0.15);
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.15) 0%, rgba(2, 229, 180, 0.05) 100%);
  border-radius: 50%;
  border: 2px solid rgba(2, 229, 180, 0.3);
  margin-bottom: 20px;
  box-shadow: 
    0 0 30px rgba(2, 229, 180, 0.2),
    inset 0 0 30px rgba(2, 229, 180, 0.05);
}

.logo-pulse {
  color: #02e5b4;
  animation: logoPulse 3s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
    filter: drop-shadow(0 0 20px rgba(2, 229, 180, 0.6));
  }
  50% {
    transform: scale(1.05);
    opacity: 0.9;
    filter: drop-shadow(0 0 40px rgba(2, 229, 180, 0.8));
  }
}

.logo-title {
  font-size: 22px;
  font-weight: bold;
  color: #fff;
  margin: 0 0 10px 0;
  letter-spacing: 2px;
}

.logo-subtitle {
  font-size: 12px;
  color: rgba(2, 229, 180, 0.6);
  margin: 0;
  letter-spacing: 4px;
  text-transform: uppercase;
}

.logo-image {
  max-width: 280px;
  height: auto;
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.1) 0%, rgba(0, 150, 255, 0.1) 100%);
  padding: 12px 20px;
  border-radius: 12px;
  border: 1px solid rgba(2, 229, 180, 0.3);
  filter: drop-shadow(0 0 20px rgba(2, 229, 180, 0.3)) drop-shadow(0 0 40px rgba(0, 150, 255, 0.2));
  transition: all 0.3s ease;
}

.logo-image:hover {
  filter: drop-shadow(0 0 30px rgba(2, 229, 180, 0.5)) drop-shadow(0 0 60px rgba(0, 150, 255, 0.3));
  border-color: rgba(2, 229, 180, 0.5);
  transform: scale(1.15);
}

.title {
  text-align: center;
  margin-bottom: 35px;
  color: #02e5b4;
  font-size: 24px;
  font-weight: 600;
  margin-top: 0;
  letter-spacing: 3px;
}

/* 表单容器 */
.login-form {
  margin: 0;
  padding: 0;
}

/* 表单项 */
.login-form .el-form-item {
  margin-bottom: 22px;
}

.login-form .el-form-item__content {
  width: 100%;
}

/* 输入框包装器 */
.input-wrapper {
  position: relative;
  width: 100%;
}

.custom-input {
  width: 100%;
  background: rgba(0, 0, 0, 0.5) !important;
  border: 1px solid rgba(2, 229, 180, 0.25) !important;
  border-radius: 10px !important;
  color: #fff !important;
  transition: all 0.3s ease !important;
  height: 52px !important;
  line-height: 52px !important;
  font-size: 15px !important;
}

.custom-input::placeholder {
  color: #555 !important;
}

.custom-input:hover {
  border-color: rgba(2, 229, 180, 0.5) !important;
  box-shadow: 0 0 20px rgba(2, 229, 180, 0.15) !important;
}

.custom-input:focus {
  border-color: rgba(2, 229, 180, 0.9) !important;
  box-shadow: 
    0 0 25px rgba(2, 229, 180, 0.35),
    inset 0 0 25px rgba(2, 229, 180, 0.08) !important;
  outline: none !important;
}

.custom-input .el-input__wrapper {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.custom-input .el-input__inner {
  background: transparent !important;
  border: none !important;
  color: #fff !important;
  font-size: 15px !important;
}

.custom-input .el-input__prefix {
  color: rgba(2, 229, 180, 0.6) !important;
}

.input-glow {
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #02e5b4, transparent);
  transition: width 0.3s ease;
  z-index: 10;
}

.custom-input:focus-within + .input-glow {
  width: 85%;
}

/* 按钮项 */
.login-form .btn-item {
  margin-bottom: 0;
  padding: 0;
}

/* 登录按钮 */
.login-btn {
  width: 100% !important;
  height: 54px !important;
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.25) 0%, rgba(2, 229, 180, 0.12) 100%) !important;
  border: 1px solid rgba(2, 229, 180, 0.6) !important;
  border-radius: 10px !important;
  color: #02e5b4 !important;
  font-size: 17px !important;
  font-weight: 600 !important;
  position: relative !important;
  overflow: hidden !important;
  transition: all 0.3s ease !important;
  margin-top: 10px !important;
  padding: 0 !important;
  letter-spacing: 4px;
}

.login-btn:hover {
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.35) 0%, rgba(2, 229, 180, 0.18) 100%) !important;
  box-shadow: 
    0 0 35px rgba(2, 229, 180, 0.45),
    inset 0 0 35px rgba(2, 229, 180, 0.12) !important;
  transform: translateY(-2px) !important;
}

.login-btn:active {
  transform: translateY(0) !important;
}

.login-btn .el-button__text {
  position: relative;
  z-index: 1;
}

.btn-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(2, 229, 180, 0.15) 50%,
    transparent 70%
  );
  transform: rotate(45deg);
  animation: btnShine 3s ease-in-out infinite;
}

@keyframes btnShine {
  0% {
    left: -50%;
  }
  100% {
    left: 150%;
  }
}

.footer {
  text-align: center;
  margin-top: 30px;
  color: #666;
  padding-top: 15px;
  font-size: 14px;
}

.register-link {
  color: #02e5b4 !important;
  text-decoration: none;
  margin-left: 6px;
  position: relative;
  transition: all 0.3s ease;
  cursor: pointer;
  font-weight: 500;
}

.register-link:hover {
  color: #02ffc8 !important;
  text-shadow: 0 0 12px rgba(2, 229, 180, 0.8);
}

.register-link::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #02e5b4, #02ffc8);
  transition: width 0.3s ease;
}

.register-link:hover::after {
  width: 100%;
}

/* 验证码容器 */
.captcha-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
}

.captcha-input {
  flex: 1;
}

.captcha-field {
  width: 100% !important;
}

.captcha-img {
  width: 130px;
  height: 52px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid rgba(2, 229, 180, 0.3);
  transition: all 0.3s ease;
}

.captcha-img:hover {
  border-color: rgba(2, 229, 180, 0.6);
  box-shadow: 0 0 15px rgba(2, 229, 180, 0.3);
  transform: scale(1.02);
}

/* 角色选择器 */
.role-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  gap: 15px;
}

.role-label {
  color: rgba(2, 229, 180, 0.8);
  font-size: 14px;
  font-weight: 500;
}

.role-options {
  display: flex;
  gap: 25px;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 25px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(2, 229, 180, 0.2);
  transition: all 0.3s ease;
  color: #888;
}

.role-option:hover {
  border-color: rgba(2, 229, 180, 0.5);
  background: rgba(2, 229, 180, 0.05);
}

.role-option.active {
  background: rgba(2, 229, 180, 0.15);
  border-color: rgba(2, 229, 180, 0.6);
  color: #02e5b4;
  box-shadow: 0 0 20px rgba(2, 229, 180, 0.25);
}

.role-option input[type="radio"] {
  display: none;
}

.role-icon {
  font-size: 18px;
}

.role-text {
  font-size: 14px;
  font-weight: 500;
}

.role-option.active .role-icon {
  animation: iconPulse 1.5s ease-in-out infinite;
}

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}
</style>
