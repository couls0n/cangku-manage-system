<template>
  <div class="login-page">
    <div class="login-hero">
      <div class="badge">仓储业务 + eBPF 安全联防</div>
      <h1>仓储后台管理中心</h1>
      <p>
        将仓库、库存、订单、伙伴管理与安全监控整合到一个后台中。管理员可直接查看 API 越权拦截、频控告警和 eBPF
        上报的系统异常事件。
      </p>
      <div class="hero-points">
        <span>统一身份令牌</span>
        <span>水平 / 垂直越权防御</span>
        <span>eBPF 异常行为可视化</span>
      </div>
    </div>

    <el-card class="login-card data-card">
      <div class="login-title">
        <h2>登录系统</h2>
        <p>默认管理员账号 `admin / 123456`</p>
      </div>
      <el-form ref="loginForm" :model="loginForm" :rules="rules" @submit.native.prevent>
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" prefix-icon="el-icon-user" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            prefix-icon="el-icon-lock"
            placeholder="请输入密码"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登录并进入后台</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      loginForm: {
        username: 'admin',
        password: '123456'
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) {
          return
        }
        this.loading = true
        try {
          const res = await request.post('/auth/login', this.loginForm)
          this.$store.dispatch('login', res.data)
          this.$message.success(`欢迎回来，${res.data.user.realName || res.data.user.username}`)
          this.$router.push('/dashboard')
        } catch (error) {
          console.error(error)
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: grid;
  grid-template-columns: 1.2fr 0.85fr;
  gap: 28px;
  padding: 36px;
  background:
    radial-gradient(circle at top left, rgba(8, 59, 102, 0.14), transparent 28%),
    radial-gradient(circle at bottom right, rgba(217, 119, 6, 0.14), transparent 26%),
    linear-gradient(135deg, #eef4fb, #f7f2eb);
}

.login-hero {
  border-radius: 28px;
  padding: 42px;
  color: #fff;
  background: linear-gradient(145deg, #082f49, #0f766e 55%, #d97706);
  box-shadow: 0 28px 50px rgba(8, 47, 73, 0.25);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.badge {
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 13px;
  font-weight: 600;
}

.login-hero h1 {
  margin: 22px 0 14px;
  font-size: 48px;
  line-height: 1.08;
}

.login-hero p {
  margin: 0;
  max-width: 620px;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.8;
}

.hero-points {
  margin-top: 28px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-points span {
  padding: 10px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
}

.login-card {
  align-self: center;
  padding: 24px;
}

.login-title h2 {
  margin: 0;
  font-size: 30px;
}

.login-title p {
  margin: 8px 0 24px;
  color: #6b7280;
}

.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 14px;
  font-size: 15px;
}
</style>
