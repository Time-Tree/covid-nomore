import React from 'react';

import Main from './Main';
import Search from './Search';
import Results from './Results';

const Test = props => {
  return <Search {...props} result="negative" />;
};

export default Test;
