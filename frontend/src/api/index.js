import http from '../plugins/http'

export const testHttp = () => {
    return http({
        url: '/nothing',
        method: 'get',
    })
}