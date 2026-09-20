import request from '@/utils/request'

export function getLikedPosts() {
  return request({
    url: '/forum/posts/liked',
    method: 'get'
  })
}

export function likePost(postId) {
  return request({
    url: `/forum/posts/${postId}/like`,
    method: 'post'
  })
}
