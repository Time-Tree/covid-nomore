import PSIClient from '@openmined/psi.js/combined/js/es';

let client;

const initializeClient = async () => {
  if (!client) {
    const psi = await PSIClient();
    client = psi.client.createWithNewKey();
  }

  return client;
};

export async function getRequest(inputs) {
  const c = await initializeClient();
  const request = c.createRequest(inputs);
  return request;
}

export async function getIntersection(data) {
  const c = await initializeClient();
  const intersection = c.getIntersectionSize(
    data.serverSetup,
    data.serverResponse
  );
  return intersection;
}
