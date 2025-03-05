#!/usr/bin/env python3
"""
File Uploader Script

This script scans a directory for files and uploads them to a specified endpoint via POST requests.
It provides progress tracking and error handling.

Usage:
    python file_uploader.py --directory /path/to/files --url http://your-api-endpoint/upload
    
Optional arguments:
    --extensions jpg,png,pdf  # Only upload files with these extensions
    --recursive               # Scan subdirectories recursively
    --threads 4               # Number of concurrent upload threads (default: 4)
    --timeout 30              # Request timeout in seconds (default: 30)
    --headers                 # Additional headers in key:value format
"""

import argparse
import os
import sys
import time
import requests
import concurrent.futures
from tqdm import tqdm
import logging
from urllib.parse import urljoin

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler("file_uploader.log"),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

def parse_arguments():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(description='Upload files from a directory to an API endpoint')
    parser.add_argument('--directory', required=True, help='Directory containing files to upload')
    parser.add_argument('--url', required=True, help='API endpoint URL')
    parser.add_argument('--extensions', help='Comma-separated list of file extensions to include (e.g., jpg,png,pdf)')
    parser.add_argument('--recursive', action='store_true', help='Scan subdirectories recursively')
    parser.add_argument('--threads', type=int, default=4, help='Number of concurrent upload threads')
    parser.add_argument('--timeout', type=int, default=30, help='Request timeout in seconds')
    parser.add_argument('--headers', nargs='*', help='Additional headers in key:value format')
    return parser.parse_args()

def get_files(directory, extensions=None, recursive=False):
    """
    Get a list of files from the specified directory.
    
    Args:
        directory (str): Directory path to scan
        extensions (list): List of file extensions to include
        recursive (bool): Whether to scan subdirectories recursively
        
    Returns:
        list: List of file paths
    """
    files = []
    
    if extensions:
        extensions = [ext.lower() if ext.startswith('.') else f'.{ext.lower()}' for ext in extensions]
    
    if recursive:
        for root, _, filenames in os.walk(directory):
            for filename in filenames:
                file_path = os.path.join(root, filename)
                if extensions is None or os.path.splitext(filename)[1].lower() in extensions:
                    files.append(file_path)
    else:
        for filename in os.listdir(directory):
            file_path = os.path.join(directory, filename)
            if os.path.isfile(file_path):
                if extensions is None or os.path.splitext(filename)[1].lower() in extensions:
                    files.append(file_path)
    
    return files

def upload_file(file_path, url, headers=None, timeout=30):
    """
    Upload a file to the specified URL.
    
    Args:
        file_path (str): Path to the file to upload
        url (str): API endpoint URL
        headers (dict): Additional headers to include in the request
        timeout (int): Request timeout in seconds
        
    Returns:
        tuple: (success, response_or_error)
    """
    try:
        filename = os.path.basename(file_path)
        file_size = os.path.getsize(file_path)
        
        logger.debug(f"Uploading {filename} ({file_size} bytes)")
        
        with open(file_path, 'rb') as file:
            files = {'file': (filename, file, 'application/octet-stream')}
            response = requests.post(
                url,
                files=files,
                headers=headers,
                timeout=timeout
            )
        
        if response.status_code in (200, 201):
            return True, response
        else:
            return False, f"HTTP {response.status_code}: {response.text}"
    
    except Exception as e:
        return False, str(e)

def main():
    """Main function."""
    args = parse_arguments()
    
    # Validate directory
    if not os.path.isdir(args.directory):
        logger.error(f"Directory not found: {args.directory}")
        sys.exit(1)
    
    # Parse extensions
    extensions = None
    if args.extensions:
        extensions = args.extensions.split(',')
    
    # Parse headers
    headers = {}
    if args.headers:
        for header in args.headers:
            if ':' in header:
                key, value = header.split(':', 1)
                headers[key.strip()] = value.strip()
    
    # Get files
    files = get_files(args.directory, extensions, args.recursive)
    
    if not files:
        logger.warning(f"No files found in {args.directory}")
        sys.exit(0)
    
    logger.info(f"Found {len(files)} files to upload")
    
    # Upload files
    success_count = 0
    error_count = 0
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=args.threads) as executor:
        # Create a dictionary to track futures
        future_to_file = {
            executor.submit(upload_file, file_path, args.url, headers, args.timeout): file_path
            for file_path in files
        }
        
        # Process results as they complete
        with tqdm(total=len(files), desc="Uploading files") as progress_bar:
            for future in concurrent.futures.as_completed(future_to_file):
                file_path = future_to_file[future]
                filename = os.path.basename(file_path)
                
                try:
                    success, result = future.result()
                    
                    if success:
                        logger.info(f"Successfully uploaded {filename}")
                        success_count += 1
                    else:
                        logger.error(f"Failed to upload {filename}: {result}")
                        error_count += 1
                
                except Exception as e:
                    logger.error(f"Error processing {filename}: {str(e)}")
                    error_count += 1
                
                progress_bar.update(1)
    
    # Print summary
    logger.info(f"Upload complete: {success_count} successful, {error_count} failed")
    
    if error_count > 0:
        logger.info("Check file_uploader.log for details on failed uploads")

if __name__ == "__main__":
    main()
