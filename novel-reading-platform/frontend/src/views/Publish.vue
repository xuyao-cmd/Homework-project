<template>
  <div class="page-wrap">
    <el-card class="publish-card">
      <div slot="header" class="publish-header">
        <div>
          <b>小说投稿</b>
          <span>提交后进入平台管理员审核，通过后才会在首页展示。</span>
        </div>
        <el-button type="primary" :loading="loading" @click="submit">提交审核</el-button>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
        <el-row :gutter="26">
          <el-col :span="7">
            <div class="cover-panel">
              <div class="cover-preview" :style="coverStyle">
                <div v-if="!form.cover" class="cover-empty">
                  <i class="el-icon-picture-outline"></i>
                  <span>本地导入封面</span>
                </div>
              </div>
              <input ref="coverInput" type="file" accept="image/*" class="file-input" @change="handleCoverChange" />
              <el-button icon="el-icon-upload2" @click="$refs.coverInput.click()">选择本地图片</el-button>
              <el-button v-if="form.cover" type="text" @click="form.cover = ''">移除封面</el-button>
              <p>支持本地 JPG/PNG 等图片，提交时会作为封面预览保存。</p>
            </div>
          </el-col>

          <el-col :span="17">
            <el-form-item label="小说标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入小说标题"></el-input>
            </el-form-item>
            <el-row :gutter="14">
              <el-col :span="12">
                <el-form-item label="分类" prop="category">
                  <el-select v-model="form.category" placeholder="请选择分类" @change="handleCategoryChange">
                    <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="二级分类" prop="subCategory">
                  <el-select v-model="form.subCategory" placeholder="请选择二级分类">
                    <el-option v-for="item in subCategoryOptions" :key="item" :label="item" :value="item"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="小说简介" prop="summary">
              <el-input v-model="form.summary" type="textarea" :rows="4" placeholder="请输入小说简介"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="chapter-section">
          <div class="section-title">
            <div>
              <b>章节内容</b>
              <span>可以继续添加第二章、第三章，至少保留一章。</span>
            </div>
            <el-button size="mini" type="primary" icon="el-icon-plus" @click="addChapter">添加章节</el-button>
          </div>

          <el-card v-for="(chapter, index) in form.chapters" :key="chapter.key" class="chapter-editor" shadow="never">
            <div slot="header" class="chapter-header">
              <b>第 {{ index + 1 }} 章</b>
              <el-button v-if="form.chapters.length > 1" size="mini" type="text" @click="removeChapter(index)">删除</el-button>
            </div>
            <el-form-item :label="`标题`" :prop="`chapters.${index}.title`" :rules="chapterTitleRule">
              <el-input v-model="chapter.title" placeholder="例如：第一章 初遇"></el-input>
            </el-form-item>
            <el-form-item :label="`正文`" :prop="`chapters.${index}.content`" :rules="chapterContentRule">
              <el-input v-model="chapter.content" type="textarea" :rows="8" placeholder="请输入章节正文"></el-input>
            </el-form-item>
          </el-card>
        </div>

        <div class="form-actions">
          <el-button type="primary" :loading="loading" @click="submit">提交审核</el-button>
          <el-button @click="$router.push('/home')">返回首页</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { submitNovel } from '../api/novel'
import { categoryOptions, getSubCategories } from '../utils/categories'

export default {
  name: 'Publish',
  data() {
    return {
      loading: false,
      categoryOptions,
      chapterTitleRule: [{ required: true, message: '请输入章节标题', trigger: 'blur' }],
      chapterContentRule: [{ required: true, message: '请输入章节正文', trigger: 'blur' }],
      form: {
        title: '',
        authorId: 1,
        category: '玄幻',
        subCategory: '东方玄幻',
        cover: '',
        summary: '',
        chapters: [
          { key: Date.now(), title: '第一章 开始', content: '' }
        ]
      },
      rules: {
        title: [{ required: true, message: '请输入小说标题', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        subCategory: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
        summary: [{ required: true, message: '请输入小说简介', trigger: 'blur' }]
      }
    }
  },
  computed: {
    subCategoryOptions() {
      return getSubCategories(this.form.category)
    },
    coverStyle() {
      return this.form.cover ? { backgroundImage: `url(${this.form.cover})` } : {}
    }
  },
  created() {
    const user = JSON.parse(localStorage.getItem('novel-user') || '{}')
    this.form.authorId = user.id || 1
  },
  methods: {
    handleCategoryChange() {
      const options = getSubCategories(this.form.category)
      this.form.subCategory = options[0] || ''
    },
    handleCoverChange(event) {
      const file = event.target.files[0]
      if (!file) return
      const reader = new FileReader()
      reader.onload = e => {
        this.form.cover = e.target.result
      }
      reader.readAsDataURL(file)
      event.target.value = ''
    },
    addChapter() {
      const next = this.form.chapters.length + 1
      this.form.chapters.push({
        key: Date.now() + next,
        title: `第${next}章 `,
        content: ''
      })
    },
    removeChapter(index) {
      this.form.chapters.splice(index, 1)
    },
    submit() {
      this.$refs.formRef.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          await submitNovel(this.form)
          this.$message.success('提交成功，等待平台管理员审核')
          this.$router.push('/user')
        } catch (e) {
          // error already shown by axios interceptor
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.publish-card {
  border-radius: 12px;
  border: 0;
}
.publish-header,
.section-title,
.chapter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.publish-header span,
.section-title span,
.cover-panel p {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}
.cover-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.cover-preview {
  width: 220px;
  height: 300px;
  border-radius: 10px;
  background: linear-gradient(135deg, #5b8def, #6bbf8a);
  background-size: cover;
  background-position: center;
  box-shadow: 0 12px 28px rgba(31, 45, 61, 0.14);
}
.cover-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #fff;
}
.cover-empty i {
  font-size: 40px;
}
.file-input {
  display: none;
}
.chapter-section {
  margin-top: 20px;
}
.section-title {
  margin-bottom: 14px;
}
.chapter-editor {
  margin-bottom: 16px;
  border-radius: 10px;
}
.form-actions {
  margin-left: 96px;
  padding-top: 8px;
}
</style>
