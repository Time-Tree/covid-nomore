// import PSIClient from '@openmined/psi.js/client/js/es';

let client;

const initializeClient = async () => {
  if (!client) {
    // const psi = await PSIClient();
    // client = psi.client.createWithNewKey();
  }

  return client;
};

export async function getRequest(inputs) {
  // const c = await initializeClient();
  // return c.createRequest(inputs);
}

export async function getIntersection(data) {
  // const c = await initializeClient();
  // return c.getIntersectionSize(data.serverSetup, data.serverResponse);
}
