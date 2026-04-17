import {
  mockExperts,
  mockProjects,
  mockSkills,
  mockDomains,
  mockDashboardStats,
  mockExpertStats,
  mockProjectStats,
  mockSkillStats,
  mockUserProfile,
} from './mock-data';
import {
  ExpertDetail,
  ProjectDetail,
  SkillDetail,
  DomainDetail,
  DashboardStats,
  ExpertStats,
  ProjectStats,
  SkillStats,
  UserProfile,
  CreateExpertRequest,
  UpdateExpertRequest,
  CreateProjectRequest,
  UpdateProjectRequest,
  CreateSkillRequest,
  UpdateSkillRequest,
  CreateDomainRequest,
  UpdateDomainRequest,
  ApiResponse,
  PaginatedResponse,
  LoginRequest,
  LoginResponse,
} from '../types';

/**
 * 模拟服务
 * 当后端API不可用时，提供完整的模拟API服务
 */

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export class MockService {
  // 认证相关
  static async login(data: LoginRequest): Promise<LoginResponse> {
    await delay(500);
    
    if (data.username === 'admin' && data.password === 'admin123') {
      return {
        token: 'mock-jwt-token-1234567890',
        user: mockUserProfile,
        expiresIn: 3600,
      };
    }
    
    throw new Error('用户名或密码错误');
  }

  static async logout(): Promise<ApiResponse<void>> {
    await delay(300);
    return { success: true, message: '登出成功' };
  }

  static async getProfile(): Promise<UserProfile> {
    await delay(300);
    return mockUserProfile;
  }

  // 专家相关
  static async getExperts(params?: any): Promise<PaginatedResponse<ExpertDetail>> {
    await delay(500);
    
    let filteredExperts = [...mockExperts];
    
    // 简单过滤逻辑
    if (params?.keyword) {
      const keyword = params.keyword.toLowerCase();
      filteredExperts = filteredExperts.filter(
        expert => 
          expert.name.toLowerCase().includes(keyword) ||
          expert.title.toLowerCase().includes(keyword) ||
          expert.location.toLowerCase().includes(keyword)
      );
    }
    
    if (params?.status) {
      filteredExperts = filteredExperts.filter(expert => expert.status === params.status);
    }
    
    if (params?.location) {
      filteredExperts = filteredExperts.filter(expert => expert.location === params.location);
    }
    
    const page = params?.page || 1;
    const pageSize = params?.pageSize || 10;
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    
    return {
      items: filteredExperts.slice(startIndex, endIndex),
      total: filteredExperts.length,
      page,
      pageSize,
      totalPages: Math.ceil(filteredExperts.length / pageSize),
    };
  }

  static async getExpertById(id: number): Promise<ExpertDetail> {
    await delay(300);
    
    const expert = mockExperts.find(e => e.id === id);
    if (!expert) {
      throw new Error(`专家ID ${id} 不存在`);
    }
    
    return expert;
  }

  static async createExpert(data: CreateExpertRequest): Promise<ExpertDetail> {
    await delay(500);
    
    const newExpert: ExpertDetail = {
      id: mockExperts.length + 1,
      ...data,
      avatar: data.avatar || `https://randomuser.me/api/portraits/men/${mockExperts.length + 1}.jpg`,
      status: 'active',
      rating: 4.0,
      projects: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    
    mockExperts.push(newExpert);
    return newExpert;
  }

  static async updateExpert(id: number, data: UpdateExpertRequest): Promise<ExpertDetail> {
    await delay(500);
    
    const index = mockExperts.findIndex(e => e.id === id);
    if (index === -1) {
      throw new Error(`专家ID ${id} 不存在`);
    }
    
    mockExperts[index] = {
      ...mockExperts[index],
      ...data,
      updatedAt: new Date().toISOString(),
    };
    
    return mockExperts[index];
  }

  static async deleteExpert(id: number): Promise<ApiResponse<void>> {
    await delay(300);
    
    const index = mockExperts.findIndex(e => e.id === id);
    if (index === -1) {
      throw new Error(`专家ID ${id} 不存在`);
    }
    
    mockExperts.splice(index, 1);
    return { success: true, message: '删除成功' };
  }

  // 项目相关
  static async getProjects(params?: any): Promise<PaginatedResponse<ProjectDetail>> {
    await delay(500);
    
    let filteredProjects = [...mockProjects];
    
    if (params?.keyword) {
      const keyword = params.keyword.toLowerCase();
      filteredProjects = filteredProjects.filter(
        project => 
          project.name.toLowerCase().includes(keyword) ||
          project.description.toLowerCase().includes(keyword)
      );
    }
    
    if (params?.status) {
      filteredProjects = filteredProjects.filter(project => project.status === params.status);
    }
    
    if (params?.priority) {
      filteredProjects = filteredProjects.filter(project => project.priority === params.priority);
    }
    
    const page = params?.page || 1;
    const pageSize = params?.pageSize || 10;
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    
    return {
      items: filteredProjects.slice(startIndex, endIndex),
      total: filteredProjects.length,
      page,
      pageSize,
      totalPages: Math.ceil(filteredProjects.length / pageSize),
    };
  }

  static async getProjectById(id: number): Promise<ProjectDetail> {
    await delay(300);
    
    const project = mockProjects.find(p => p.id === id);
    if (!project) {
      throw new Error(`项目ID ${id} 不存在`);
    }
    
    return project;
  }

  static async createProject(data: CreateProjectRequest): Promise<ProjectDetail> {
    await delay(500);
    
    const newProject: ProjectDetail = {
      id: mockProjects.length + 1,
      ...data,
      progress: 0,
      manager: data.manager || '系统管理员',
      experts: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    
    mockProjects.push(newProject);
    return newProject;
  }

  static async updateProject(id: number, data: UpdateProjectRequest): Promise<ProjectDetail> {
    await delay(500);
    
    const index = mockProjects.findIndex(p => p.id === id);
    if (index === -1) {
      throw new Error(`项目ID ${id} 不存在`);
    }
    
    mockProjects[index] = {
      ...mockProjects[index],
      ...data,
      updatedAt: new Date().toISOString(),
    };
    
    return mockProjects[index];
  }

  static async deleteProject(id: number): Promise<ApiResponse<void>> {
    await delay(300);
    
    const index = mockProjects.findIndex(p => p.id === id);
    if (index === -1) {
      throw new Error(`项目ID ${id} 不存在`);
    }
    
    mockProjects.splice(index, 1);
    return { success: true, message: '删除成功' };
  }

  // 技能相关
  static async getSkills(params?: any): Promise<PaginatedResponse<SkillDetail>> {
    await delay(500);
    
    let filteredSkills = [...mockSkills];
    
    if (params?.keyword) {
      const keyword = params.keyword.toLowerCase();
      filteredSkills = filteredSkills.filter(
        skill => 
          skill.name.toLowerCase().includes(keyword) ||
          skill.category.toLowerCase().includes(keyword) ||
          skill.description.toLowerCase().includes(keyword)
      );
    }
    
    if (params?.category) {
      filteredSkills = filteredSkills.filter(skill => skill.category === params.category);
    }
    
    if (params?.level) {
      filteredSkills = filteredSkills.filter(skill => skill.level === params.level);
    }
    
    const page = params?.page || 1;
    const pageSize = params?.pageSize || 10;
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    
    return {
      items: filteredSkills.slice(startIndex, endIndex),
      total: filteredSkills.length,
      page,
      pageSize,
      totalPages: Math.ceil(filteredSkills.length / pageSize),
    };
  }

  static async getSkillById(id: number): Promise<SkillDetail> {
    await delay(300);
    
    const skill = mockSkills.find(s => s.id === id);
    if (!skill) {
      throw new Error(`技能ID ${id} 不存在`);
    }
    
    return skill;
  }

  static async createSkill(data: CreateSkillRequest): Promise<SkillDetail> {
    await delay(500);
    
    const newSkill: SkillDetail = {
      id: mockSkills.length + 1,
      ...data,
      popularity: 50,
      experts: [],
      projects: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    
    mockSkills.push(newSkill);
    return newSkill;
  }

  static async updateSkill(id: number, data: UpdateSkillRequest): Promise<SkillDetail> {
    await delay(500);
    
    const index = mockSkills.findIndex(s => s.id === id);
    if (index === -1) {
      throw new Error(`技能ID ${id} 不存在`);
    }
    
    mockSkills[index] = {
      ...mockSkills[index],
      ...data,
      updatedAt: new Date().toISOString(),
    };
    
    return mockSkills[index];
  }

  static async deleteSkill(id: number): Promise<ApiResponse<void>> {
    await delay(300);
    
    const index = mockSkills.findIndex(s => s.id === id);
    if (index === -1) {
      throw new Error(`技能ID ${id} 不存在`);
    }
    
    mockSkills.splice(index, 1);
    return { success: true, message: '删除成功' };
  }

  // 领域相关
  static async getDomains(params?: any): Promise<PaginatedResponse<DomainDetail>> {
    await delay(500);
    
    let filteredDomains = [...mockDomains];
    
    if (params?.keyword) {
      const keyword = params.keyword.toLowerCase();
      filteredDomains = filteredDomains.filter(
        domain => 
          domain.name.toLowerCase().includes(keyword) ||
          domain.description.toLowerCase().includes(keyword)
      );
    }
    
    if (params?.parentId !== undefined) {
      filteredDomains = filteredDomains.filter(domain => domain.parentId === params.parentId);
    }
    
    const page = params?.page || 1;
    const pageSize = params?.pageSize || 10;
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    
    return {
      items: filteredDomains.slice(startIndex, endIndex),
      total: filteredDomains.length,
      page,
      pageSize,
      totalPages: Math.ceil(filteredDomains.length / pageSize),
    };
  }

  static async getDomainById(id: number): Promise<DomainDetail> {
    await delay(300);
    
    const domain = mockDomains.find(d => d.id === id);
    if (!domain) {
      throw new Error(`领域ID ${id} 不存在`);
    }
    
    return domain;
  }

  static async createDomain(data: CreateDomainRequest): Promise<DomainDetail> {
    await delay(500);
    
    const newDomain: DomainDetail = {
      id: mockDomains.length + 1,
      ...data,
      level: data.parentId ? 2 : 1,
      experts: [],
      projects: [],
      skills: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    
    mockDomains.push(newDomain);
    return newDomain;
  }

  static async updateDomain(id: number, data: UpdateDomainRequest): Promise<DomainDetail> {
    await delay(500);
    
    const index = mockDomains.findIndex(d => d.id === id);
    if (index === -1) {
      throw new Error(`领域ID ${id} 不存在`);
    }
    
    mockDomains[index] = {
      ...mockDomains[index],
      ...data,
      updatedAt: new Date().toISOString(),
    };
    
    return mockDomains[index];
  }

  static async deleteDomain(id: number): Promise<ApiResponse<void>> {
    await delay(300);
    
    const index = mockDomains.findIndex(d => d.id === id);
    if (index === -1) {
      throw new Error(`领域ID ${id} 不存在`);
    }
    
    mockDomains.splice(index, 1);
    return { success: true, message: '删除成功' };
  }

  // 统计相关
  static async getDashboardStats(): Promise<DashboardStats> {
    await delay(500);
    return mockDashboardStats;
  }

  static async getExpertStats(): Promise<ExpertStats> {
    await delay(300);
    return mockExpertStats;
  }

  static async getProjectStats(): Promise<ProjectStats> {
    await delay(300);
    return mockProjectStats;
  }

  static async getSkillStats(): Promise<SkillStats> {
    await delay(300);
    return mockSkillStats;
  }
}