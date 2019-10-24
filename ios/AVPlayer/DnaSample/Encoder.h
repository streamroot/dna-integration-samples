//
//  Encoder.h
//  iOS-Demo
//
//  Created by Lamine Ndiaye on 24/10/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface Encoder: NSObject
+ (NSString *)urlencodeWithString: (NSString*) string;
@end

NS_ASSUME_NONNULL_END
