<template>
  <div class="register-container">
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
    
    <div class="register-box">
      <!-- Logo区域 -->
      <div class="logo-section">
        <img src="/logo.png" alt="财报智析" class="logo-image" />
      </div>
      
      <!-- 标题 -->
      <h2 class="title">用户注册</h2>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="100px" class="register-form">
        <el-form-item label="用户名" prop="username">
          <div class="input-wrapper">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              class="custom-input"
            />
            <div class="input-glow"></div>
          </div>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <div class="input-wrapper">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              prefix-icon="Message"
              class="custom-input"
            />
            <div class="input-glow"></div>
          </div>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <div class="input-wrapper">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              prefix-icon="Phone"
              class="custom-input"
            />
            <div class="input-glow"></div>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <div class="input-wrapper">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              class="custom-input"
            />
            <div class="input-glow"></div>
          </div>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <div class="input-wrapper">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              show-password
              class="custom-input"
            />
            <div class="input-glow"></div>
          </div>
        </el-form-item>
        <el-form-item class="btn-item">
          <el-button type="primary" @click="handleRegister" :loading="loading" class="register-btn">
            <span class="btn-text">注 册</span>
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { register } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
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

onMounted(() => {
  generateParticles()
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register(registerForm)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
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

.register-box {
  width: 520px;
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
.register-form {
  margin: 0;
  padding: 0;
}

/* 表单项 */
.register-form .el-form-item {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.register-form :deep(.el-form-item__label) {
  width: 100px;
  text-align: right;
  padding-right: 15px;
  color: #ffffff !important;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.register-form .el-form-item__content {
  flex: 1;
  min-width: 0;
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
  height: 50px !important;
  line-height: 50px !important;
  font-size: 14px !important;
}

.custom-input::placeholder {
  color: #555 !important;
}

.custom-input:hover {
  border-color: rgba(2, 229, 180, 0.5) !important;
  box-shadow: 0 0 15px rgba(2, 229, 180, 0.15) !important;
}

.custom-input:focus {
  border-color: rgba(2, 229, 180, 0.9) !important;
  box-shadow: 
    0 0 20px rgba(2, 229, 180, 0.3),
    inset 0 0 20px rgba(2, 229, 180, 0.08) !important;
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
  font-size: 14px !important;
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
  width: 80%;
}

/* 按钮项 */
.register-form .btn-item {
  margin-bottom: 0;
  padding: 0;
  justify-content: center;
}

/* 注册按钮 */
.register-btn {
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

.register-btn:hover {
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.35) 0%, rgba(2, 229, 180, 0.18) 100%) !important;
  box-shadow: 
    0 0 35px rgba(2, 229, 180, 0.45),
    inset 0 0 35px rgba(2, 229, 180, 0.12) !important;
  transform: translateY(-2px) !important;
}

.register-btn:active {
  transform: translateY(0) !important;
}

.register-btn .el-button__text {
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

.login-link {
  color: #02e5b4 !important;
  text-decoration: none;
  margin-left: 6px;
  position: relative;
  transition: all 0.3s ease;
  cursor: pointer;
  font-weight: 500;
}

.login-link:hover {
  color: #02ffc8 !important;
  text-shadow: 0 0 12px rgba(2, 229, 180, 0.8);
}

.login-link::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #02e5b4, #02ffc8);
  transition: width 0.3s ease;
}

.login-link:hover::after {
  width: 100%;
}
</style>