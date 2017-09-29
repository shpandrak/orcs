const userApi = {
  users:          `${APISERVER}/hub-web-api/user`,
  loggedInUser:   `${APISERVER}/hub-web-api/logged-in-user`,
  avatar:         id => { return `${APISERVER}/hub-web-api/user/${id}/avatar` }
}

export default userApi;
