/**
 * Metro configuration for React Native
 * https://github.com/facebook/react-native
 *
 * @format
 */

const blacklist = require('metro-config/src/defaults/blacklist');

module.exports = {
  transformer: {
    getTransformOptions: async () => ({
      transform: {
        experimentalImportSupport: false,
        inlineRequires: false
      }
    })
  },
  resolver: {
    blacklistRE: blacklist([
      // Ignore local `.sample.js` files.
      /.*\.sample\.js$/,
      // Ignore IntelliJ directories
      /.*\.idea\/.*/,
      // ignore git directories
      /.*\.git\/.*/,
      // Ignore android directories
      /.*\/app\/build\/.*/

      // Add more regexes here for paths which should be blacklisted from the packager.
    ])
  }
};
