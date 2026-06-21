<template>
  <div class="page-wrap" v-loading="loading">
    <el-row :gutter="22">
      <el-col :span="7">
        <el-card class="profile-card">
          <div class="profile-banner"></div>
          <div class="profile-top" v-if="profile">
            <el-avatar :size="92" :src="profile.avatar"></el-avatar>
            <h2>{{ profile.nickname }}</h2>
            <el-tag size="mini" type="success">普通用户</el-tag>
            <div class="meta-line">用户名：{{ profile.username }}</div>
            <p class="summary">{{ profile.introduction }}</p>
          </div>
          <div class="profile-stats">
            <div><b>{{ shelf.length }}</b><span>书架</span></div>
            <div><b>{{ profile ? profile.followCount : 0 }}</b><span>关注</span></div>
            <div><b>{{ profile ? profile.publishCount : 0 }}</b><span>投稿</span></div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="17">
        <el-card class="center-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="我的书架" name="shelf">
              <div class="shelf-header">
                <div>
                  <b>最近收藏</b>
                  <span>登录后加入书架的作品会保存在这里</span>
                </div>
                <el-button size="mini" type="primary" plain @click="$router.push('/books')">发现更多</el-button>
              </div>
              <el-row :gutter="16" v-if="shelf.length">
                <el-col :span="12" v-for="item in shelf" :key="item.id">
                  <div class="shelf-item">
                    <div class="shelf-cover" :class="coverClass(item.category)">
                      <span>{{ cleanTitle(item.title).slice(0, 4) }}</span>
                    </div>
                    <div class="shelf-info">
                      <h3>{{ cleanTitle(item.title) }}</h3>
                      <p>{{ displayAuthorName(item.authorName, item.title) }} · {{ item.subCategory }}</p>
                      <div class="shelf-actions">
                        <el-button size="mini" type="primary" @click="continueRead(item)">继续阅读</el-button>
                        <el-button size="mini" @click="removeBook(item.id)">移除</el-button>
                      </div>
                    </div>
                  </div>
                </el-col>
              </el-row>
              <el-empty v-else description="书架还是空的，去详情页加入喜欢的小说吧"></el-empty>
            </el-tab-pane>

            <el-tab-pane label="编辑资料" name="profile">
              <el-form :model="form" label-width="96px" class="profile-form">
                <el-form-item label="昵称">
                  <el-input v-model="form.nickname"></el-input>
                </el-form-item>
                <el-form-item label="头像地址">
                  <el-input v-model="form.avatar"></el-input>
                </el-form-item>
                <el-form-item label="个人简介">
                  <el-input type="textarea" :rows="5" v-model="form.introduction"></el-input>
                </el-form-item>
                <el-button type="primary" :loading="saving" @click="save">保存资料</el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getProfile, updateProfile } from '../api/user'
import { getChapters } from '../api/novel'
import { cleanTitle, displayAuthorName } from '../utils/format'
import { getShelf, removeFromShelf } from '../utils/shelf'

export default {
  name: 'UserCenter',
  data() {
    return {
      activeTab: 'shelf',
      loading: false,
      saving: false,
      profile: null,
      shelf: [],
      form: {
        nickname: '',
        avatar: '',
        introduction: ''
      }
    }
  },
  created() {
    this.loadProfile()
    this.loadShelf()
  },
  methods: {
    cleanTitle,
    displayAuthorName,
    currentUserId() {
      const user = JSON.parse(localStorage.getItem('novel-user') || '{}')
      return user.id || 1
    },
    loadShelf() {
      this.shelf = getShelf()
    },
    async loadProfile() {
      this.loading = true
      try {
        const res = await getProfile(this.currentUserId())
        this.profile = res.data
        this.form.nickname = this.profile.nickname
        this.form.avatar = this.profile.avatar
        this.form.introduction = this.profile.introduction
      } catch (e) {
        this.profile = null
      } finally {
        this.loading = false
      }
    },
    async save() {
      this.saving = true
      try {
        const res = await updateProfile(this.currentUserId(), this.form)
        this.profile = res.data
        localStorage.setItem('novel-user', JSON.stringify(res.data))
        this.$emit('login-success')
        this.$message.success('保存成功')
      } catch (e) {
        // error already shown by axios interceptor
      } finally {
        this.saving = false
      }
    },
    async continueRead(item) {
      try {
        const res = await getChapters(item.id)
        const first = (res.data || [])[0]
        if (first) {
          this.$router.push(`/read/${first.novelId}/${first.id}`)
        }
      } catch (e) {
        // error already shown by axios interceptor
      }
    },
    removeBook(id) {
      removeFromShelf(id)
      this.loadShelf()
      this.$message.success('已移出书架')
    },
    coverClass(category) {
      return {
        fantasy: category === '玄幻',
        history: category === '历史',
        city: category === '都市',
        other: category === '其他'
      }
    }
  }
}
</script>

<style scoped>
.profile-card,
.center-card {
  border-radius: 12px;
  border: 0;
  overflow: hidden;
}
.profile-card {
  text-align: center;
}
.profile-banner {
  height: 96px;
  margin: -20px -20px 0;
  background: linear-gradient(135deg, #5b8def 0%, #6bbf8a 100%);
}
.profile-top {
  margin-top: -46px;
}
.profile-top h2 {
  margin: 14px 0 8px;
}
.profile-top .summary {
  min-height: 50px;
}
.profile-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 22px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
.profile-stats b {
  display: block;
  color: #3f73c8;
  font-size: 24px;
}
.profile-stats span {
  color: #909399;
  font-size: 13px;
}
.shelf-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.shelf-header b {
  display: block;
  margin-bottom: 4px;
  font-size: 18px;
}
.shelf-header span {
  color: #909399;
  font-size: 13px;
}
.shelf-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fff;
}
.shelf-cover {
  width: 86px;
  height: 112px;
  border-radius: 8px;
  display: flex;
  align-items: flex-end;
  padding: 12px;
  box-sizing: border-box;
  color: #fff;
  font-weight: 700;
}
.shelf-cover.fantasy { background: linear-gradient(135deg, #5b8def, #7aa7d9); }
.shelf-cover.history { background: linear-gradient(135deg, #d9a24a, #c58f63); }
.shelf-cover.city { background: linear-gradient(135deg, #6bbf8a, #4aa3a2); }
.shelf-cover.other { background: linear-gradient(135deg, #8d79c7, #7f8ca3); }
.shelf-info {
  flex: 1;
  min-width: 0;
}
.shelf-info h3 {
  margin: 4px 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.shelf-info p {
  margin: 0 0 18px;
  color: #606266;
}
.shelf-actions {
  display: flex;
  gap: 8px;
}
.profile-form {
  max-width: 760px;
  padding-top: 10px;
}
</style>
