import {axios} from '../utils/request'
import {COMMENT_MODULE} from "@/api/_prefix";

type CommentInfo = {
    productId: number,
    username: string | null,
    content: string,
    rating: number
}

export const postComments = (comment: CommentInfo) => {
    return axios.post(COMMENT_MODULE, comment)
        .then(res => {
            console.log("添加评论成功：", res);
            return res;
        })
        .catch(error => {
            console.error('添加评论失败', error);
            throw error;
        });
}

export const getComments = (productId: number) => {
    return axios.get(`${COMMENT_MODULE}/product/${productId}`)
        .then(res => {
            console.log("get comment:", res);
            return res;
        });
};

export const deleteComment = (commentId: number) => {
    return axios.delete(`${COMMENT_MODULE}/${commentId}`).then(res => {
        console.log(res)
    });
}