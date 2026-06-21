export function cleanTitle(title) {
  return (title || '').replace(/\d+$/, '')
}

const fallbackAuthors = [
  '林照晚', '沈听澜', '顾云舟', '叶知秋', '苏见微', '温折柳', '陆星河', '白鹿眠', '秦疏影', '许青岚',
  '江月白', '宋栖迟', '周南絮', '闻人雪', '谢临川', '洛沉璧', '唐砚初', '纪微雨', '夏安歌', '程望舒'
]

export function displayAuthorName(authorName, title) {
  if (authorName && authorName !== '平台管理员' && authorName !== '匿名作者') {
    return authorName
  }
  const source = title || ''
  let hash = 0
  for (let i = 0; i < source.length; i += 1) {
    hash = (hash + source.charCodeAt(i) * (i + 1)) % fallbackAuthors.length
  }
  return fallbackAuthors[hash]
}
