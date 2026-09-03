<template>
  <footer class="app-footer">
    <div class="footer-container">
      <!-- 顶部：4列链接 -->
      <div class="footer-cols">
        <div class="footer-col">
          <h4>商城</h4>
          <ul>
            <li><a href="#">关于我们</a></li>
            <li><a href="#">联系我们</a></li>
            <li><a href="#">企业责任</a></li>
          </ul>
        </div>
        <div class="footer-col">
          <h4>购物指南</h4>
          <ul>
            <li><a href="#">购物流程</a></li>
            <li><a href="#">支付方式</a></li>
            <li><a href="#">配送说明</a></li>
          </ul>
        </div>
        <div class="footer-col">
          <h4>售后服务</h4>
          <ul>
            <li><a href="#">退换货政策</a></li>
            <li><a href="#">价格保护</a></li>
            <li><a href="#">帮助中心</a></li>
          </ul>
        </div>
        <div class="footer-col">
          <h4>法律</h4>
          <ul>
            <li><a href="#">隐私政策</a></li>
            <li><a href="#">服务条款</a></li>
            <li><a href="#">网站地图</a></li>
          </ul>
        </div>
      </div>
      
      <!-- 底部：版权信息 -->
      <div class="footer-bottom">
        <p>Copyright © 2024 Mall Inc. 保留所有权利。</p>
        <p class="footer-extra">
          <a href="#">隐私政策</a>
          <span class="divider">|</span>
          <a href="#">使用条款</a>
          <span class="divider">|</span>
          <a href="#">销售政策</a>
          <span class="divider">|</span>
          <a href="#">法律</a>
        </p>
      </div>
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.app-footer {
  background: #f5f5f7;
  padding: 48px 0 24px;
  margin-top: 80px;
  font-size: 13px;
  color: #6e6e73;
}

.footer-container {
  max-width: 1024px;
  margin: 0 auto;
  padding: 0 22px;
}

.footer-cols {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid #d2d2d7;
  
  @media (max-width: 833px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
  
  h4 {
    font-size: 13px;
    font-weight: 600;
    color: #1d1d1f;
    margin-bottom: 12px;
  }
  
  ul {
    list-style: none;
    
    li {
      margin-bottom: 8px;
    }
    
    a {
      color: #6e6e73;
      transition: color 0.2s;
      
      &:hover {
        color: #1d1d1f;
      }
    }
  }
}

.footer-bottom {
  padding-top: 24px;
  text-align: center;
  
  p {
    margin-bottom: 8px;
    font-size: 12px;
  }
}

.footer-extra {
  a {
    color: #6e6e73;
    margin: 0 8px;
    
    &:hover {
      color: #1d1d1f;
    }
  }
  
  .divider {
    color: #d2d2d7;
  }
}
</style>
