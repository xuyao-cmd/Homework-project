import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

const originalPush = Router.prototype.push
Router.prototype.push = function push(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalPush.call(this, location, onResolve, onReject)
  }
  return originalPush.call(this, location).catch(error => {
    if (error && error.name === 'NavigationDuplicated') {
      return error
    }
    return Promise.reject(error)
  })
}

const router = new Router({
  mode: 'hash',
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/home', name: 'Home', component: () => import('../views/Home.vue') },
    { path: '/books', name: 'Books', component: () => import('../views/Books.vue') },
    { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
    { path: '/detail/:id', name: 'Detail', component: () => import('../views/Detail.vue') },
    { path: '/read/:novelId/:chapterId', name: 'Read', component: () => import('../views/Read.vue') },
    { path: '/publish', name: 'Publish', component: () => import('../views/Publish.vue') },
    { path: '/user', name: 'UserCenter', component: () => import('../views/UserCenter.vue') },
    { path: '/admin', name: 'Admin', component: () => import('../views/Admin.vue') }
  ]
})

router.beforeEach((to, from, next) => {
  const needLogin = ['/publish', '/user', '/admin'].includes(to.path) || to.path.indexOf('/read/') === 0
  const raw = localStorage.getItem('novel-user')
  const user = raw ? JSON.parse(raw) : null
  const isAdmin = user && (user.role === 'ADMIN' || user.username === 'admin')
  if (needLogin && !user) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/admin' && !isAdmin) {
    next('/home')
  } else if (['/publish', '/user'].includes(to.path) && isAdmin) {
    next('/admin')
  } else {
    next()
  }
})

export default router
