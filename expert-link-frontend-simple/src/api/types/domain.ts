/**
 * 领域基本信息
 */
export interface DomainBase {
  id: number;
  name: string;
  description: string;
  icon?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 领域详情
 */
export interface DomainDetail extends DomainBase {
  parentId?: number;
  parentName?: string;
  children: DomainBase[];
  expertCount: number;
  projectCount: number;
  requiredSkills: DomainSkill[];
}

/**
 * 领域技能关联
 */
export interface DomainSkill {
  id: number;
  skillId: number;
  skillName: string;
  importance: number; // 1-5
}

/**
 * 创建领域请求
 */
export interface CreateDomainRequest {
  name: string;
  description: string;
  parentId?: number;
  icon?: string;
  skillIds?: number[];
}

/**
 * 更新领域请求
 */
export interface UpdateDomainRequest extends Partial<CreateDomainRequest> {
  id: number;
}

/**
 * 领域查询参数
 */
export interface DomainQueryParams {
  name?: string;
  parentId?: number;
  page?: number;
  size?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

/**
 * 领域树节点
 */
export interface DomainTreeNode {
  id: number;
  name: string;
  children: DomainTreeNode[];
  expertCount: number;
  projectCount: number;
}