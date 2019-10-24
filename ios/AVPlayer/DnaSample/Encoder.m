//
//  Encoder.m
//  iOS-Demo
//
//  Created by Lamine Ndiaye on 24/10/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//

#import "Encoder.h"

@implementation Encoder

+ (NSString *)urlencodeWithString: (NSString*) string {
  NSMutableString *output = [NSMutableString string];
  const unsigned char *source = (const unsigned char *)[string UTF8String];
  int sourceLen = strlen((const char *)source);
  for (int i = 0; i < sourceLen; ++i) {
    const unsigned char thisChar = source[i];
    if (thisChar == ' '){
        [output appendString:@"+"];
        } else if (thisChar == '.' || thisChar == '-' || thisChar == '_' || thisChar == '~' ||
                   (thisChar >= 'a' && thisChar <= 'z') ||
                   (thisChar >= 'A' && thisChar <= 'Z') ||
                   (thisChar >= '0' && thisChar <= '9')) {
          [output appendFormat:@"%c", thisChar];
        } else {
          [output appendFormat:@"%%%02X", thisChar];
        }
        }
        return output;
        }
        
@end
