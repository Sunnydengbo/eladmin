import request from '@/utils/request'

export function login(username, password, code, uuid) {
  return request({
    url: 'auth/login',
    method: 'post',
    data: {
      username,
      password,
      code,
      uuid
    }
  })
}

export function getInfo() {
  return request({
    url: 'auth/info',
    method: 'get'
  })
}

export function getCodeImg(uuid) {
  const params = {
    uuid: uuid
  }
  return request({
    url: 'auth/code',
    method: 'get',
    params
  })
}

export function logout() {
  return request({
    url: 'auth/logout',
    method: 'delete'
  })
}
