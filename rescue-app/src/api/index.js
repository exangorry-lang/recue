import { get, post } from '@/utils/request'

// ===== 认证 =====
export const login = (data) => post('/auth/login', data)

// ===== 分级训练 =====
export const getProjectPage = (data) => get('/train/project/page', data)
export const getProjectDetail = (id) => get(`/train/project/${id}`)
export const submitCheckin = (data) => post('/train/checkin', data)

// ===== 理论学习 =====
export const getQuestionPage = (data) => get('/exam/question/page', data)

// ===== 考核 =====
export const getSessionList = (data) => get('/exam/session/list', data)
export const startExam = (id) => post(`/exam/session/${id}/start`)
export const submitExam = (recordId, data) => post(`/exam/record/${recordId}/submit`, data)
export const getMyScore = () => get('/exam/score/my')

// ===== 团队 =====
export const getMyGroups = () => get('/team/group/my')
export const getTaskList = () => get('/team/task/list')
export const submitTeamRecord = (data) => post('/team/record', data)

// ===== 档案 =====
export const getMyArchive = () => get('/archive/my')
export const applyPromotion = (data) => post('/archive/promotion', data)
export const getMyPromotions = () => get('/archive/promotion/list')
