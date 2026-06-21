export const categoryOptions = [
  { label: '玄幻', value: '玄幻', children: ['东方玄幻', '异世大陆', '修真仙侠'] },
  { label: '历史', value: '历史', children: ['架空历史', '朝堂权谋', '古风传奇'] },
  { label: '都市', value: '都市', children: ['都市生活', '职场商战', '技术创业'] },
  { label: '其他', value: '其他', children: ['悬疑推理', '轻小说', '科幻未来'] }
]

export function getSubCategories(category) {
  const option = categoryOptions.find(item => item.value === category)
  return option ? option.children : []
}
