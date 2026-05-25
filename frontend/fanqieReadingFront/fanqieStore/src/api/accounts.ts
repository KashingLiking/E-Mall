import { axios } from '../utils/request'
import { USER_MODULE } from './_prefix'
import {AxiosResponse} from "axios";

// 用户信息结构同步
type UserResponse = {
    username: string;
    name: string;
    email?: string;
    telephone?: string;
    location?: string;
};

type LoginInfo = {
    username: string,
    password: string
}

// 更新RegisterInfo类型
type RegisterInfo = {
    username: string;
    password: string;
    name: string;
    role: string;
    avatar?: string;
    telephone?: string;
    email?: string;
    location?: string;
}
type UpdateInfo = {
    name?: string,
    password?: string,
    location?: string,
}

type ForgotInfo = {
    username: string,
    telephone: string,
    email: string,
}
// 严格对应接口返回结构
export interface UserDetail {
    username: string;
    name: string;
    role: 'admin' | 'user';
    avatar?: string;
    telephone?: string;
    email?: string;
    location?: string;
}

export interface ApiResponse<T = any> {
    code: string;
    msg: string | null;
    data: T;
}


export const login = (loginInfo: LoginInfo) => {
    return axios.post(`${USER_MODULE}/login`, loginInfo,
        {headers: { 'Content-Type': 'application/json' }
        }
    ).then(res => {
        // 假设返回格式：{ code: 200, data: "token_string" }
        if (res.data.code == 200) {
            return res.data.data; // 返回token字符串
        }
        throw new Error(res.data.msg || '登录失败');
    });
}
// 注册接口

export const register = (registerInfo: RegisterInfo) => {
    // 注意：后端没有role字段，前端不要发送
    const { username, password, ...rest } = registerInfo;

    return axios.post(`${USER_MODULE}`, {
        username,
        password: password, // 前端加密
        ...rest
    }, {
        headers: { 'Content-Type': 'application/json' }
    }).then(res => {
        console.log(res.data);
        if (res.data.code !== '200' && res.data.code !== '200') {
            throw new Error(res.data.msg || '注册失败')
        }
        return res.data
    })
}
// 获取用户信息
export const getUserInfo = (userId: string) => {
    return axios.get<UserResponse>(`${USER_MODULE}/${userId}`)
        .then(res => {
            return res.data
        })
}

// 修改更新接口调用
export const updateUser = (updateInfo: UpdateInfo) => {
    console.log("updatainfo in accounts:" + updateInfo.name)
    return axios.put(USER_MODULE, updateInfo, {
        headers: {
            'Content-Type': 'application/json',
            'Token': sessionStorage.getItem('token'),
        }
    })
}
// 找回密码
export const forgotPassword = (forgotInfo: ForgotInfo) => {
    return axios.post(`${USER_MODULE}/forgot-password`, forgotInfo, {
        headers: { 'Content-Type': 'application/json' }
    })
        .then(res => {
            return res;
        });
};



/**
 * 获取用户详情服务
 * @param username 路径参数用户名
 * @param token 认证令牌
 * @throws 当HTTP状态码非2xx时抛出错误
 */
export const fetchUserDetail = async (
    username: string,
    token: string
): Promise<ApiResponse<UserDetail>> => {
    console.log(token);
    const response: AxiosResponse<ApiResponse<UserDetail>> = await axios({
        method: 'GET',
        url: `/api/accounts/${username}`,
        headers: {
            'Token': token
        },

    });

    if (response.status === 400) {
        throw new Error('身份凭证已失效，请重新登录');
    }

    if (response.data.code !== '200') {
        throw new Error(response.data.msg || '用户数据获取失败');
    }

    return response.data;
};
// api/accounts.ts
export const getUserDetail = (username: string) => {
    return axios.get(`/api/accounts/${username}`, {
        headers: {
            token: sessionStorage.getItem('token')
        }
    })
}
