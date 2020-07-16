import axios from 'axios';

const API_URL = 'https://covid-no-more-be.herokuapp.com';

const SERVER_TIMEOUT = 15000;

export default function request(method, url, data) {
  return new Promise((resolve, reject) => {
    const axiosRequest = {
      baseURL: `${API_URL}/${url}`,
      method,
      data,
      timeout: SERVER_TIMEOUT
    };
    axios.request(axiosRequest).then(
      data => {
        resolve(data);
      },
      err => {
        reject(err);
      }
    );
  });
}
