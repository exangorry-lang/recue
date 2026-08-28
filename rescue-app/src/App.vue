<script>
export default {
  onLaunch() {
    // 启动时检查登录状态
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  }
}
</script>

<style>
page {
  background-color: #F5F7FA;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  color: #333333;
}

/* 品牌色变量 */
:root, page {
  --primary: #005A9C;
  --primary-grad: #0072BD;
  --text-main: #333333;
  --text-sub: #666666;
  --success: #09B865;
  --warning: #FF7D00;
  --danger: #F53F3F;
  --disabled: #CCCCCC;
  --page-bg: #F5F7FA;
  --card-bg: #FFFFFF;
}

/* 通用卡片 */
.card {
  background: #FFFFFF;
  border-radius: 12rpx;
  padding: 24rpx;
  margin: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 16rpx;
}
</style>
