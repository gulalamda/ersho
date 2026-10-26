<template>
  <div class="shell nf">
    <p class="nf__code num">404</p>
    <h1 class="nf__title">这个摊位上什么都没有</h1>
    <p class="nf__hint">地址可能写错了，也可能这件东西已经被人买走了。</p>
    <RouterLink class="nf__action" :to="{ name: 'market' }">回到集市</RouterLink>
  </div>
</template>

<style scoped>
.nf {
  padding-top: 40px;
  padding-bottom: 80px;
}

.nf__code {
  font-size: 64px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.03em;
  color: var(--line-strong);
}

.nf__title {
  margin-top: 14px;
  font-size: 24px;
  font-weight: 800;
}

.nf__hint {
  margin-top: 8px;
  font-size: 15px;
  color: var(--ink-soft);
}

.nf__action {
  display: inline-block;
  margin-top: 22px;
  padding: 9px 18px;
  font-size: 14px;
  font-weight: 700;
  color: var(--card);
  background: var(--board);
  border-radius: var(--r-sm);
}

.nf__action:hover {
  background: var(--board-deep);
}
</style>
