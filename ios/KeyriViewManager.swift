import keyri_pod

@objc(KeyriViewManager)
class KeyriViewManager: RCTViewManager {

    override func view() -> (KeyriView) {
        return KeyriView()
    }
}

class KeyriView : UIView {
    
    private var keyri = Keyri()
    
    @objc var color: String = "" {
        didSet {
            self.backgroundColor = hexStringToUIColor(hexColor: color)
        }
    }

    func hexStringToUIColor(hexColor: String) -> UIColor {
        let stringScanner = Scanner(string: hexColor)

        if(hexColor.hasPrefix("#")) {
            stringScanner.scanLocation = 1
        }
        var color: UInt32 = 0
        stringScanner.scanHexInt32(&color)

        let r = CGFloat(Int(color >> 16) & 0x000000FF)
        let g = CGFloat(Int(color >> 8) & 0x000000FF)
        let b = CGFloat(Int(color) & 0x000000FF)

        return UIColor(red: r / 255.0, green: g / 255.0, blue: b / 255.0, alpha: 1)
    }
    
    @objc
    func initialize(appkey: String, rpPublicKey: String, callbackUrl: String) {
        guard let callbackUrl = URL(string: callbackUrl) else { return }
        Keyri.configure(appkey: appkey, rpPublicKey: rpPublicKey, callbackUrl: callbackUrl)
    }
    
    @objc
    func onReadSessionId(sessionId: String, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        keyri.onReadSessionId(sessionId) { result in
            switch result {
            case .success(let session):
                resolver(session)
            case .failure(let error):
                reject("Error", "there was error during fetching session", error)
            }
        }
    }
    
    @objc
    func keyriSignUp(username: String, service: Service, custom: String?, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        keyri.signup(username: username, service: service, custom: custom) { (result: Result<Void, Error>) in
            switch result {
            case .success():
                resolver("Success, user is registered")
            case .failure(let error):
                reject("Error", "there was error during registration", error)
            }
        }
    }
    
    @objc
    func keyriLogin(account: PublicAccount, service: Service, custom: String?, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        keyri.login(account: account, service: service, custom: custom) { (result: Result<Void, Error>) in
            switch result {
            case .success():
                resolver("Success, user is logged in")
            case .failure(let error):
                reject("Error", "there was error during login", error)
            }
        }
    }
    
    @objc
    func rpDirectSignUp(username: String, custom: String?, extendedHeaders: [String: String]?, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        keyri.mobileSignup(username: username, custom: custom, extendedHeaders: extendedHeaders) { (result: Result<[String : Any], Error>) in
            switch result {
            case .success(_):
                resolver("Success, user is registered via mobile signUp")
            case .failure(let error):
                reject("Error", "there was error during mobile signUp", error)
            }
        }
    }
        
    @objc
    func rpDirectLogin(account: PublicAccount, custom: String?, extendedHeaders: [String: String]?, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        keyri.mobileLogin(account: account, custom: custom, extendedHeaders: extendedHeaders) { (result: Result<[String : Any], Error>) in
            switch result {
            case .success(_):
                resolver("Success, user is loggedin via mobile login")
            case .failure(let error):
                reject("Error", "there was error during mobile login", error)
            }
        }
    }
        
    @objc
    func accounts(resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        keyri.accounts { (result: Result<[PublicAccount], Error>) in
            switch result {
            case .success(let accounts):
                resolver(accounts)
            case .failure(let error):
                reject("Error", "there was error during fetching accounts", error)
            }
        }
    }
        
    @objc
    func easyKeyriAuth(custom: String?, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        keyri.authWithScanner(from: nil, custom: custom) { (result: Result<Void, Error>) in
            switch result {
            case .success():
                resolver("Success")
            case .failure(let error):
                reject("Error", "there was error during auth with scanner", error)
            }
        }
    }
}
