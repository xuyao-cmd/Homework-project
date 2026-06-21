function getCurrentUser() {
  return JSON.parse(localStorage.getItem('novel-user') || 'null')
}

function shelfKey(userId) {
  return `novel-shelf-${userId}`
}

export function currentUser() {
  return getCurrentUser()
}

export function isLoggedIn() {
  return !!getCurrentUser()
}

export function getShelf() {
  const user = getCurrentUser()
  if (!user) {
    return []
  }
  return JSON.parse(localStorage.getItem(shelfKey(user.id)) || '[]')
}

export function isInShelf(novelId) {
  return getShelf().some(item => String(item.id) === String(novelId))
}

export function addToShelf(novel) {
  const user = getCurrentUser()
  if (!user || !novel) {
    return false
  }
  const shelf = getShelf()
  if (!shelf.some(item => String(item.id) === String(novel.id))) {
    shelf.unshift({
      id: novel.id,
      title: novel.title,
      authorName: novel.authorName,
      category: novel.category,
      subCategory: novel.subCategory,
      score: novel.score,
      clickCount: novel.clickCount,
      summary: novel.summary,
      cover: novel.cover,
      addedAt: new Date().toISOString()
    })
    localStorage.setItem(shelfKey(user.id), JSON.stringify(shelf))
  }
  return true
}

export function removeFromShelf(novelId) {
  const user = getCurrentUser()
  if (!user) {
    return
  }
  const shelf = getShelf().filter(item => String(item.id) !== String(novelId))
  localStorage.setItem(shelfKey(user.id), JSON.stringify(shelf))
}
