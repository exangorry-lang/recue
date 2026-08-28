import request from '@/utils/request'

// ===== 认证 =====
export const login = (data) => request.post('/auth/login', data)
export const getMe = () => request.get('/auth/me')

// ===== 用户 =====
export const getUserPage = (params) => request.get('/sys/user/page', { params })
export const createUser = (data) => request.post('/sys/user', data)
export const updateUser = (data) => request.put('/sys/user', data)
export const deleteUser = (id) => request.delete(`/sys/user/${id}`)
export const updateUserStatus = (id, enabled) => request.put(`/sys/user/${id}/status`, null, { params: { enabled } })
export const updateUserLevel = (id, level) => request.put(`/sys/user/${id}/level`, null, { params: { level } })
export const resetPassword = (id) => request.put(`/sys/user/${id}/password`)

// ===== 部门 =====
export const getDeptList = () => request.get('/sys/dept/list')
export const createDept = (data) => request.post('/sys/dept', data)
export const updateDept = (data) => request.put('/sys/dept', data)
export const deleteDept = (id) => request.delete(`/sys/dept/${id}`)

// ===== 角色 =====
export const getRoleList = () => request.get('/sys/role/list')

// ===== 训练项目 =====
export const getProjectPage = (params) => request.get('/train/project/page', { params })
export const importProjects = (data) => request.post('/import/train-project', data)

// ===== 题库 =====
export const getQuestionPage = (params) => request.get('/exam/question/page', { params })
export const importQuestions = (data) => request.post('/import/question', data)

// ===== 考试 =====
export const getSessionList = (params) => request.get('/exam/session/list', { params })
export const createSession = (data) => request.post('/exam/session', data)

// ===== 打卡审核 =====
export const getReviewPage = (params) => request.get('/train/checkin/review', { params })
export const reviewCheckin = (id, data) => request.post(`/train/checkin/${id}/review`, data)

// ===== 团队 =====
export const getTeamList = () => request.get('/team/group/list')
export const createTeam = (data) => request.post('/team/group', data)
export const getTaskList = () => request.get('/team/task/list')
export const createTask = (data) => request.post('/team/task', data)

// ===== 晋升 =====
export const getPromotionList = () => request.get('/archive/promotion/list')
export const getPendingPromotions = () => request.get('/archive/promotion/pending')
export const approvePromotion = (id, data) => request.post(`/archive/promotion/${id}/approve`, data)

// ===== 统计 =====
export const getStatsOverview = () => request.get('/stats/overview')
