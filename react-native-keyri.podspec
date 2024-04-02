require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |spec|
  spec.name         = package['name']
  spec.version      = package["version"]
  spec.summary      = package["description"]
  spec.homepage     = package["homepage"]
  spec.license      = package["license"]
  spec.authors      = package["author"]

  spec.platforms    = { :ios => "14.0" }
  spec.source       = { :git => "https://github.com/Keyri-Co/react-native-keyri.git", :tag => "#{spec.version}" }

  spec.source_files = "ios/**/*.{h,m,mm,swift}"

  spec.dependency "React-Core"
  spec.dependency "keyri-pod", '~> 4.6.0-alpha05'
end
