function defaultHeaders(){
    const token = localStorage.getItem("token");

    return{
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
        //'Authorization': 'Bearer ' + token,
};
}

export default defaultHeaders;